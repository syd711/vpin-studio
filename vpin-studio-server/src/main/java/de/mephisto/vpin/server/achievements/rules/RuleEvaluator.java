package de.mephisto.vpin.server.achievements.rules;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Port of the Achievement Watcher's {@code _evaluate_achievements} (global rules) and
 * {@code _evaluate_player_session_achievements} (per ROM rules) for one player and one game.
 * <p>
 * Differences to the Watcher (see plan §8/§10):
 * <ul>
 *   <li>nvram_tally counts only deltas of tracked games, no absolute cabinet audit sums</li>
 *   <li>every stage reached in a game is awarded (the Watcher's VPX 10.8.1 behaviour), not only the highest one</li>
 *   <li>only "P1 ..", "P2 .." player fields are skipped for session deltas, not every field starting with "P"</li>
 *   <li>rom_complete_set achievements are not revoked when new tables are installed</li>
 *   <li>per ROM rules are skipped for multiplayer games</li>
 * </ul>
 */
public class RuleEvaluator {

  public record Award(String title, String scope) {
  }

  public static class Input {
    public String rom;
    public Map<String, Object> startAudits = Collections.emptyMap();
    public Map<String, Object> endAudits = Collections.emptyMap();
    public long durationSec;
    public boolean multiplayer;
    public List<AchievementRule> globalRules = Collections.emptyList();
    public List<AchievementRule> romRules = Collections.emptyList();
    /** ROM -> normalized manufacturer, may return null */
    public Function<String, String> manufacturerOfRom = r -> null;
    /** manufacturer (or "__any__") -> installed ROMs with a NVRAM map */
    public Function<String, Set<String>> installedRoms = m -> Collections.emptySet();
  }

  public static class Result {
    public final List<Award> awards = new ArrayList<>();
    public final List<String> retriggered = new ArrayList<>();
    /** session deltas (> 0) used for the per ROM rules, also used for the "best delta" progress */
    public Map<String, Long> sessionDeltas = new LinkedHashMap<>();
  }

  private RuleEvaluator() {
  }

  @NonNull
  public static Result evaluate(@NonNull Input in, @NonNull PlayerProgress progress) {
    Result result = new Result();
    if (in.rom != null && !in.rom.isEmpty()) {
      progress.romsPlayed.add(in.rom);
    }
    evaluateGlobal(in, progress, result);
    result.sessionDeltas = computeSessionDeltas(in.startAudits, in.endAudits);
    if (!in.multiplayer) {
      evaluateSession(in, progress, result);
    }
    return result;
  }

  private static void evaluateGlobal(Input in, PlayerProgress progress, Result result) {
    Map<String, Long> deltasCi = new LinkedHashMap<>();
    for (String k : in.endAudits.keySet()) {
      long d = intCi(in.endAudits, k) - intCi(in.startAudits, k);
      deltasCi.put(k, Math.max(0, d));
    }
    Map<String, String> mfrCache = new HashMap<>();
    Function<String, String> mfr = r -> mfrCache.computeIfAbsent(r, x -> {
      String v = in.manufacturerOfRom.apply(x);
      return v == null ? "" : v;
    });

    Set<String> seen = new HashSet<>();
    for (AchievementRule rule : in.globalRules) {
      if (!rule.isGlobal()) {
        continue;
      }
      String title = rule.getTitleOrDefault();
      if (!seen.add(title)) {
        continue;
      }
      RuleCondition cond = rule.getCondition();
      if (cond == null || cond.getType() == null) {
        continue;
      }
      boolean already = progress.unlockedTitles.contains(title);
      boolean met = false;
      try {
        switch (cond.getType().toLowerCase()) {
          case RuleCondition.NVRAM_OVERALL -> {
            String field = cond.getField();
            if (field == null || AuditFieldRules.isExcludedField(field)) continue;
            int need = cond.minOrDefault(1);
            long sv = intCi(in.startAudits, field);
            long ev = intCi(in.endAudits, field);
            met = sv < need && need <= ev;
          }
          case RuleCondition.NVRAM_DELTA -> {
            String field = cond.getField();
            if (field == null || AuditFieldRules.isExcludedField(field)) continue;
            long d = Math.max(0, intCi(in.endAudits, field) - intCi(in.startAudits, field));
            met = d >= cond.minOrDefault(1);
          }
          case RuleCondition.NVRAM_TALLY -> {
            String field = cond.getField();
            if (field == null || AuditFieldRules.isExcludedField(field)) continue;
            long delta = fuzzySumDeltas(deltasCi, field);
            PlayerProgress.Tally tally = progress.globalTally.computeIfAbsent(title, t -> new PlayerProgress.Tally());
            if (!already && delta > 0) {
              tally.progress += delta;
            }
            met = tally.progress >= cond.minOrDefault(1);
          }
          case RuleCondition.ROM_COUNT -> {
            String manufacturer = cond.getManufacturer() == null ? "" : cond.getManufacturer();
            long count;
            int need;
            if (RuleCondition.ANY_MANUFACTURER.equals(manufacturer)) {
              if (cond.getMinBrands() != null) {
                Set<String> brands = new HashSet<>();
                for (String r : progress.romsPlayed) {
                  String b = mfr.apply(r);
                  if (!b.isEmpty()) brands.add(b);
                }
                count = brands.size();
                need = cond.getMinBrands();
              }
              else {
                count = progress.romsPlayed.size();
                need = cond.minOrDefault(1);
              }
            }
            else {
              count = progress.romsPlayed.stream().filter(r -> manufacturer.equals(mfr.apply(r))).count();
              need = cond.minOrDefault(1);
            }
            progress.globalTally.put(title, new PlayerProgress.Tally(count, null));
            met = count >= need;
          }
          case RuleCondition.ROM_COMPLETE_SET -> {
            String manufacturer = cond.getManufacturer() == null ? "" : cond.getManufacturer();
            Set<String> installed = in.installedRoms.apply(manufacturer);
            if (installed == null || installed.isEmpty()) continue;
            long played = installed.stream().filter(progress.romsPlayed::contains).count();
            progress.globalTally.put(title, new PlayerProgress.Tally(played, installed.size()));
            met = played >= installed.size();
          }
          case RuleCondition.ROM_MULTI_BRAND -> {
            List<String> manufacturers = cond.getManufacturers() != null ? cond.getManufacturers() : List.of();
            Set<String> playedBrands = new HashSet<>();
            for (String r : progress.romsPlayed) {
              String b = mfr.apply(r);
              if (!b.isEmpty()) playedBrands.add(b);
            }
            long count = manufacturers.stream().filter(playedBrands::contains).count();
            progress.globalTally.put(title, new PlayerProgress.Tally(count, manufacturers.size()));
            met = count >= manufacturers.size();
          }
          default -> {
          }
        }
      }
      catch (Exception e) {
        continue;
      }
      if (met) {
        if (already) {
          result.retriggered.add(title);
        }
        else {
          result.awards.add(new Award(title, AchievementRule.SCOPE_GLOBAL));
          progress.unlockedTitles.add(title);
        }
      }
    }
  }

  private static void evaluateSession(Input in, PlayerProgress progress, Result result) {
    Set<String> seen = new LinkedHashSet<>();
    for (AchievementRule rule : in.romRules) {
      String title = rule.getTitleOrDefault();
      if (!seen.add(title)) {
        continue;
      }
      RuleCondition cond = rule.getCondition();
      if (cond == null || cond.getType() == null) {
        continue;
      }
      boolean met = false;
      if (RuleCondition.NVRAM_DELTA.equals(cond.getType())) {
        String field = cond.getField();
        if (field == null || AuditFieldRules.isExcludedField(field)) {
          continue;
        }
        met = result.sessionDeltas.getOrDefault(field, 0L) >= cond.minOrDefault(0);
      }
      else if (RuleCondition.SESSION_TIME.equals(cond.getType())) {
        met = in.durationSec >= cond.sessionSeconds();
      }
      if (met) {
        if (progress.unlockedTitles.contains(title)) {
          result.retriggered.add(title);
        }
        else {
          result.awards.add(new Award(title, "rom"));
          progress.unlockedTitles.add(title);
        }
      }
    }
  }

  /**
   * Port of {@code _compute_session_deltas}.
   */
  @NonNull
  public static Map<String, Long> computeSessionDeltas(@NonNull Map<String, Object> start, @NonNull Map<String, Object> end) {
    Map<String, Long> out = new LinkedHashMap<>();
    for (Map.Entry<String, Object> e : end.entrySet()) {
      String k = e.getKey();
      if (AuditFieldRules.isPlayerField(k)) {
        continue;
      }
      if (AuditFieldRules.isExcludedField(k) || AuditFieldRules.DELTA_IGNORE.contains(k.toLowerCase())) {
        continue;
      }
      Long ev = toLong(e.getValue());
      Long sv = toLong(start.get(k));
      if (ev == null) {
        continue;
      }
      long d = ev - (sv != null ? sv : 0);
      if (d > 0) {
        out.put(k, d);
      }
    }
    return out;
  }

  /**
   * Port of {@code _fuzzy_sum_deltas}.
   */
  static long fuzzySumDeltas(Map<String, Long> deltasCi, String canonicalField) {
    long exact = deltasCi.getOrDefault(canonicalField, 0L);
    if (exact > 0) {
      return exact;
    }
    List<List<String>> patterns = AuditFieldRules.TALLY_PATTERNS.get(canonicalField);
    if (patterns == null) {
      return 0;
    }
    long total = 0;
    for (Map.Entry<String, Long> e : deltasCi.entrySet()) {
      String kl = e.getKey().toLowerCase();
      for (List<String> kws : patterns) {
        if (kws.stream().allMatch(kl::contains)) {
          total += e.getValue();
          break;
        }
      }
    }
    return total;
  }

  private static long intCi(Map<String, Object> audits, String label) {
    if (audits == null) {
      return 0;
    }
    Object v = audits.get(label);
    if (v == null) {
      for (Map.Entry<String, Object> e : audits.entrySet()) {
        if (e.getKey().equalsIgnoreCase(label)) {
          v = e.getValue();
          break;
        }
      }
    }
    Long l = toLong(v);
    return l != null ? l : 0;
  }

  @Nullable
  private static Long toLong(@Nullable Object v) {
    if (v instanceof Number n) {
      return n.longValue();
    }
    if (v instanceof String s) {
      try {
        return Long.parseLong(s.trim());
      }
      catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }
}
