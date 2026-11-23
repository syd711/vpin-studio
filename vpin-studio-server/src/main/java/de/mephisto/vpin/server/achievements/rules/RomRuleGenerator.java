package de.mephisto.vpin.server.achievements.rules;

import de.mephisto.vpin.server.achievements.nvram.MapField;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Port of the Achievement Watcher's {@code _ensure_rom_specific}: generates the per ROM ("session") achievements
 * from the fields of the NVRAM map and the decoded audits. The output must be identical to the Watcher's so that
 * migrated unlocks match (verified by golden tests).
 */
public class RomRuleGenerator {

  private static final int[] SESSION_TIME_MINUTES = {5, 10, 15, 20, 30, 45};
  private static final int MAX_SESSION_MILESTONES_PER_FIELD = 2;
  private static final int MAX_SESSION_USES_PER_FIELD = 2;
  private static final Set<String> NOT_OK = Set.of("current_player", "player_count", "current_ball", "balls played",
      "credits", "tilted", "game over", "tilt warnings");
  private static final String DASH = " – ";

  private RomRuleGenerator() {
  }

  @NonNull
  public static List<AchievementRule> generate(@NonNull String rom, @NonNull Map<String, Object> audits, @NonNull List<MapField> mapFields) {
    if (rom.isEmpty() || audits.isEmpty()) {
      return new ArrayList<>();
    }

    Set<String> prioritySet = new HashSet<>();
    List<String> priorityFields = new ArrayList<>();
    for (MapField f : mapFields) {
      String sec = f.section().toLowerCase();
      if (sec.contains("feature") || sec.contains("champion") || sec.contains("mode")) {
        String lbl = f.label();
        if (!lbl.isEmpty() && audits.containsKey(lbl)) {
          priorityFields.add(lbl);
          prioritySet.add(lbl);
        }
      }
    }

    int targetSessionTotal = Math.max(36, priorityFields.size() * 2 + 15);

    List<String> intFields = new ArrayList<>();
    for (Map.Entry<String, Object> e : audits.entrySet()) {
      if (e.getValue() instanceof Number) {
        intFields.add(e.getKey());
      }
    }
    List<String> plausible = new ArrayList<>();
    for (String k : intFields) {
      if (AuditFieldRules.isPlausibleCounter(k) && okLabel(k, prioritySet)) {
        plausible.add(k);
      }
    }

    List<String> mapFieldLabels = mapFieldLabels(mapFields);
    List<String> combined = new ArrayList<>(priorityFields);
    combined.addAll(mapFieldLabels);
    combined.addAll(plausible);
    List<String> ordered = uniq(combined);
    if (ordered.isEmpty()) ordered = mapFieldLabels;
    if (ordered.isEmpty()) ordered = plausible;
    if (ordered.isEmpty()) ordered = intFields;
    List<String> filtered = new ArrayList<>();
    for (String f : ordered) {
      if (okLabel(f, prioritySet)) {
        filtered.add(f);
      }
    }
    ordered = filtered;

    Map<String, List<String>> cats = new LinkedHashMap<>();
    for (String key : List.of("power", "precision", "progress", "meta", "other")) {
      cats.put(key, new ArrayList<>());
    }
    for (String f : ordered) {
      cats.get(category(f)).add(f);
    }

    List<String> sessionFields = new ArrayList<>();
    for (String f : priorityFields) {
      if (ordered.contains(f) && !sessionFields.contains(f)) {
        sessionFields.add(f);
      }
    }

    int targetSessionUniqueFields = Math.max(15, sessionFields.size() + 10);
    Map<String, Integer> idxs = new HashMap<>();
    cats.keySet().forEach(k -> idxs.put(k, 0));
    while (sessionFields.size() < targetSessionUniqueFields) {
      boolean progressed = false;
      for (Map.Entry<String, List<String>> e : cats.entrySet()) {
        List<String> arr = e.getValue();
        int i = idxs.get(e.getKey());
        while (i < arr.size() && sessionFields.contains(arr.get(i))) {
          i++;
        }
        idxs.put(e.getKey(), i);
        if (i < arr.size()) {
          sessionFields.add(arr.get(i));
          idxs.put(e.getKey(), i + 1);
          progressed = true;
        }
      }
      if (!progressed) {
        break;
      }
    }

    if (sessionFields.isEmpty()) {
      sessionFields = new ArrayList<>(ordered.subList(0, Math.min(ordered.size(), targetSessionUniqueFields)));
    }

    List<AchievementRule> rules = new ArrayList<>();
    Set<String> seenTitles = new HashSet<>();

    for (int mins : SESSION_TIME_MINUTES) {
      String title = uniqueTitle(rom + DASH + "Play " + mins + " Minutes", seenTitles);
      rules.add(new AchievementRule(title, RuleCondition.sessionTime(mins * 60), AchievementRule.SCOPE_SESSION));
    }

    int remainingSession = Math.max(0, targetSessionTotal - rules.size());
    Map<String, Integer> usedPerField = new HashMap<>();

    for (String fld : sessionFields) {
      if (remainingSession <= 0) {
        break;
      }
      if (fld.toLowerCase().contains("games started")) {
        continue;
      }
      List<Integer> picks = pickSessionMilestones(fld, prioritySet);
      if (picks.isEmpty()) {
        continue;
      }
      for (int m : picks) {
        if (remainingSession <= 0) {
          break;
        }
        int cnt = usedPerField.getOrDefault(fld, 0);
        if (cnt >= MAX_SESSION_USES_PER_FIELD) {
          break;
        }
        String title = uniqueTitle(rom + DASH + fld + ": " + m, seenTitles);
        rules.add(new AchievementRule(title, RuleCondition.nvramDelta(fld, m), AchievementRule.SCOPE_SESSION));
        usedPerField.put(fld, cnt + 1);
        remainingSession--;
      }
    }
    return rules;
  }

  /**
   * Port of {@code _map_fields_for_rom}.
   */
  static List<String> mapFieldLabels(List<MapField> fields) {
    List<String> out = new ArrayList<>();
    for (MapField f : fields) {
      String lbl = f.label().trim();
      if (lbl.isEmpty()) {
        continue;
      }
      if (lbl.toLowerCase().contains("score")) {
        continue;
      }
      if (AuditFieldRules.isExcludedField(lbl) || AuditFieldRules.isNoise(lbl)) {
        continue;
      }
      out.add(lbl);
    }
    Set<String> seen = new HashSet<>();
    List<String> uniq = new ArrayList<>();
    for (String x : out) {
      if (seen.add(x.toLowerCase())) {
        uniq.add(x);
      }
    }
    return uniq;
  }

  private static boolean okLabel(String lbl, Set<String> prioritySet) {
    if (lbl == null || lbl.trim().isEmpty()) {
      return false;
    }
    String ll = lbl.toLowerCase();
    boolean priority = prioritySet.contains(lbl);
    if (!priority && ll.contains("score")) {
      return false;
    }
    if (!priority && (AuditFieldRules.isExcludedField(lbl) || AuditFieldRules.isNoise(lbl))) {
      return false;
    }
    return !NOT_OK.contains(ll);
  }

  private static String category(String lbl) {
    String ll = lbl == null ? "" : lbl.toLowerCase();
    if (containsAny(ll, "extra ball", "ball save", "multiball", "jackpot", "wizard")) return "power";
    if (containsAny(ll, "ramp", "loop", "orbit", "spinner", "target", "combo")) return "precision";
    if (containsAny(ll, "mode", "lock", "locks lit", "balls locked")) return "progress";
    if (containsAny(ll, "games started", "balls played")) return "meta";
    return "other";
  }

  private static boolean containsAny(String s, String... keys) {
    for (String k : keys) {
      if (s.contains(k)) {
        return true;
      }
    }
    return false;
  }

  private static List<Integer> pickSessionMilestones(String lbl, Set<String> prioritySet) {
    String ll = lbl.toLowerCase();
    boolean priority = prioritySet.contains(lbl);
    if (priority && (ll.contains("score") || ll.contains("champion"))) {
      return List.of(1);
    }
    int cap = AuditFieldRules.sessionCap(lbl);
    List<Integer> mils = new ArrayList<>();
    for (int m : AuditFieldRules.sessionMilestones(lbl)) {
      if (m <= cap) {
        mils.add(m);
      }
    }
    if (mils.isEmpty()) {
      return priority ? List.of(1) : List.of();
    }
    if (mils.size() == 1) {
      return List.of(mils.get(0));
    }
    int low = mils.get(0);
    int mid = mils.get(Math.max(1, mils.size() / 2 - 1));
    List<Integer> picks = new ArrayList<>(new LinkedHashSet<>(List.of(low, mid)));
    return picks.subList(0, Math.min(picks.size(), MAX_SESSION_MILESTONES_PER_FIELD));
  }

  private static List<String> uniq(List<String> seq) {
    return new ArrayList<>(new LinkedHashSet<>(seq));
  }

  /**
   * Port of {@code _unique_title}.
   */
  static String uniqueTitle(String title, Set<String> seen) {
    String base = title.trim();
    if (seen.add(base)) {
      return base;
    }
    int i = 2;
    while (true) {
      String cand = base + " #" + i;
      if (seen.add(cand)) {
        return cand;
      }
      i++;
    }
  }
}
