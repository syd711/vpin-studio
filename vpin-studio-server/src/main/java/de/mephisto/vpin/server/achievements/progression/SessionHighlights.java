package de.mephisto.vpin.server.achievements.progression;

import de.mephisto.vpin.restclient.achievements.SessionHighlight;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Session highlights of the Achievement Watcher ({@code HIGHLIGHT_RULES}, {@code EVENT_KEYWORDS},
 * {@code _build_events_from_deltas}, {@code analyze_session}): events are derived from the audit deltas of a game
 * by keywords and grouped into the categories Power, Precision and Fun.
 * <p>
 * "Best Ball" is not supported, it needs live ball tracking (score-server plugin of VPX 10.8.1).
 */
public final class SessionHighlights {

  public static final String POWER = "Power";
  public static final String PRECISION = "Precision";
  public static final String FUN = "Fun";
  public static final List<String> CATEGORIES = List.of(POWER, PRECISION, FUN);

  public static final int LINES_PER_CATEGORY = 5;

  private enum Type {COUNT, FLAG}

  private record Rule(String category, String label, Type type) {
  }

  private static final Map<String, Rule> RULES = new LinkedHashMap<>();

  static {
    RULES.put("multiball", new Rule(POWER, "Multiball", Type.COUNT));
    RULES.put("jackpot", new Rule(POWER, "Jackpot Hunter", Type.COUNT));
    RULES.put("super_jackpot", new Rule(POWER, "Super Jackpot", Type.COUNT));
    RULES.put("triple_jackpot", new Rule(POWER, "Triple Jackpot", Type.COUNT));
    RULES.put("ball_save", new Rule(POWER, "Ball Saves", Type.COUNT));
    RULES.put("extra_ball", new Rule(POWER, "Extra Balls", Type.COUNT));
    RULES.put("special_award", new Rule(POWER, "Special Awards", Type.COUNT));
    RULES.put("mode_completed", new Rule(POWER, "Modes Completed", Type.COUNT));
    RULES.put("wizard_mode", new Rule(POWER, "Wizard Mode", Type.FLAG));
    RULES.put("loops", new Rule(PRECISION, "Loop Machine", Type.COUNT));
    RULES.put("spinner", new Rule(PRECISION, "Spinner Madness", Type.COUNT));
    RULES.put("combo", new Rule(PRECISION, "Combo King", Type.COUNT));
    RULES.put("drop_targets", new Rule(PRECISION, "Target Slayer", Type.COUNT));
    RULES.put("ramps", new Rule(PRECISION, "Rampage", Type.COUNT));
    RULES.put("orbit", new Rule(PRECISION, "Orbit Runner", Type.COUNT));
    RULES.put("skillshot", new Rule(PRECISION, "Skill Shot", Type.COUNT));
    RULES.put("super_skillshot", new Rule(PRECISION, "Super Skill Shot", Type.COUNT));
    RULES.put("mode_starts", new Rule(PRECISION, "Modes Started", Type.COUNT));
    RULES.put("tilt_warnings", new Rule(FUN, "Tilt Warnings", Type.COUNT));
    RULES.put("tilt", new Rule(FUN, "Tilted", Type.COUNT));
    RULES.put("devils_number", new Rule(FUN, "Devil’s Number", Type.FLAG));
    RULES.put("match", new Rule(FUN, "Match Lucky", Type.COUNT));
  }

  /**
   * Keyword lists in the order of the Watcher, the order matters for the per field attribution.
   */
  private static final Map<String, List<String>> EVENT_KEYWORDS = new LinkedHashMap<>();

  static {
    EVENT_KEYWORDS.put("super_jackpot", List.of("super jackpot", "super-jackpot", "super jp", "super jp."));
    EVENT_KEYWORDS.put("triple_jackpot", List.of("triple jackpot", "triple-jackpot", "triple jp", "triple jp."));
    EVENT_KEYWORDS.put("jackpot", List.of("jackpot", " jp", " jp.", "jackpots"));
    EVENT_KEYWORDS.put("multiball", List.of("multiball", "multi-ball", "multi ball", "multiballs", "m.b.", "mb start"));
    EVENT_KEYWORDS.put("ball_save", List.of("ball save", "ball saves"));
    EVENT_KEYWORDS.put("extra_ball", List.of("extra ball", "extra balls", "e.b.", "ex. ball"));
    EVENT_KEYWORDS.put("special_award", List.of("special"));
    EVENT_KEYWORDS.put("loops", List.of("loop", "loops"));
    EVENT_KEYWORDS.put("spinner", List.of("spinner"));
    EVENT_KEYWORDS.put("combo", List.of("combo", "combos"));
    EVENT_KEYWORDS.put("drop_targets", List.of("drop target", "targets"));
    EVENT_KEYWORDS.put("ramps", List.of("ramp", "ramps"));
    EVENT_KEYWORDS.put("orbit", List.of("orbit", "orbits"));
    EVENT_KEYWORDS.put("super_skillshot", List.of("super skill", "super skillshot"));
    EVENT_KEYWORDS.put("skillshot", List.of("skill shot", "skillshot"));
    EVENT_KEYWORDS.put("mode_completed", List.of("mode complete", "modes completed", "wave compl", "mission compl"));
    EVENT_KEYWORDS.put("mode_starts", List.of("mode start", "modes started", "wave start", "mission start", "atk. start"));
    EVENT_KEYWORDS.put("tilt_warnings", List.of("tilt warn", "tilt warning", "tilt warnings"));
    EVENT_KEYWORDS.put("tilt", List.of(" tilt ", "tilted"));
    EVENT_KEYWORDS.put("match", List.of("match awards", "match lucky"));
    EVENT_KEYWORDS.put("wizard_mode", List.of("wizard mode", "wizard", "universe start", "universe won", "ruler of the universe"));
  }

  /** fields containing one of these words are no event counters (maximums, settings, percentages) */
  private static final List<String> NOISE = List.of("max ", "count", "system", "percent", "boost", "allow", "level");

  /** words in a field name that mean "not a played multiball" (locks, lit lamps, multiball jackpots, ...) */
  private static final List<String> MB_EXCLUDE = List.of("lock", "lit", "ready", "light", "jackpot", " jp", "save", "qualif", "award", "score", "value");

  private SessionHighlights() {
  }

  /**
   * @param deltas session deltas (> 0) of the game, see {@code RuleEvaluator.computeSessionDeltas}
   * @param score  the final score of player 1, null if unknown
   * @return category -> highlights (best first, at most {@link #LINES_PER_CATEGORY}), all categories are present
   */
  @NonNull
  public static Map<String, List<SessionHighlight>> analyze(@NonNull Map<String, Long> deltas, @Nullable Long score) {
    Map<String, Long> events = events(deltas);

    Map<String, List<SessionHighlight>> buckets = new LinkedHashMap<>();
    Map<SessionHighlight, Long> weights = new LinkedHashMap<>();
    for (String category : CATEGORIES) {
      buckets.put(category, new ArrayList<>());
    }
    for (Map.Entry<String, Rule> e : RULES.entrySet()) {
      String key = e.getKey();
      Rule rule = e.getValue();
      long value;
      if ("devils_number".equals(key)) {
        value = score != null && String.valueOf(score).contains("666") ? 1 : 0;
      }
      else {
        value = events.getOrDefault(key, 0L);
      }
      if (value <= 0) {
        continue;
      }
      SessionHighlight h = new SessionHighlight();
      h.key = key;
      h.category = rule.category();
      h.label = rule.label();
      h.flag = rule.type() == Type.FLAG;
      h.value = h.flag ? 1 : value;
      buckets.get(rule.category()).add(h);
      weights.put(h, h.flag ? 150 : 100 + value);
    }
    for (List<SessionHighlight> list : buckets.values()) {
      // stable sort: equal weights keep the rule order, like Python's sorted()
      list.sort(Comparator.comparing((SessionHighlight h) -> weights.get(h)).reversed());
      if (list.size() > LINES_PER_CATEGORY) {
        list.subList(LINES_PER_CATEGORY, list.size()).clear();
      }
    }
    return buckets;
  }

  /**
   * Event counts of a game ({@code _build_events_from_deltas}).
   */
  @NonNull
  public static Map<String, Long> events(@NonNull Map<String, Long> deltas) {
    Map<String, Long> events = new LinkedHashMap<>();
    for (String key : RULES.keySet()) {
      events.put(key, 0L);
    }
    for (Map.Entry<String, Long> e : deltas.entrySet()) {
      Long val = e.getValue();
      if (val == null || val <= 0) {
        continue;
      }
      String l = e.getKey().toLowerCase();
      if (NOISE.stream().anyMatch(l::contains)) {
        continue;
      }
      List<String> matched = new ArrayList<>();
      for (Map.Entry<String, List<String>> k : EVENT_KEYWORDS.entrySet()) {
        if (k.getValue().stream().anyMatch(l::contains)) {
          matched.add(k.getKey());
        }
      }
      if (matched.contains("super_jackpot") || matched.contains("triple_jackpot")) {
        matched.remove("jackpot");
      }
      if (matched.contains("super_skillshot")) {
        matched.remove("skillshot");
      }
      if (matched.contains("mode_completed")) {
        matched.remove("mode_starts");
      }
      for (String key : matched) {
        if (!"multiball".equals(key)) {
          events.merge(key, val, Long::sum);
        }
      }
    }
    events.put("multiball", multiballCount(deltas));
    return events;
  }

  /**
   * Number of multiballs played. Tables keep several multiball audits (starts, per mode counters, totals), adding
   * them up counts one multiball several times, so the best single counter is taken, preferring audits that count
   * starts.
   */
  static long multiballCount(@NonNull Map<String, Long> deltas) {
    List<String> words = EVENT_KEYWORDS.get("multiball");
    long starts = 0;
    long others = 0;
    for (Map.Entry<String, Long> e : deltas.entrySet()) {
      Long v = e.getValue();
      if (v == null || v <= 0) {
        continue;
      }
      String l = e.getKey().toLowerCase();
      if (words.stream().noneMatch(l::contains) || MB_EXCLUDE.stream().anyMatch(l::contains)) {
        continue;
      }
      if (l.contains("start") || l.contains("played") || l.contains("total")) {
        starts = Math.max(starts, v);
      }
      else {
        others = Math.max(others, v);
      }
    }
    return starts > 0 ? starts : others;
  }

  /**
   * The final score of player 1 ({@code _find_score_from_audits}): "P1 Score", else "Score".
   */
  @Nullable
  public static Long score(@Nullable Map<String, Object> audits) {
    if (audits == null) {
      return null;
    }
    Long score = toLong(audits.get("P1 Score"));
    return score != null ? score : toLong(audits.get("Score"));
  }

  @Nullable
  private static Long toLong(@Nullable Object value) {
    if (value instanceof Number n) {
      return n.longValue();
    }
    if (value != null) {
      try {
        return Long.parseLong(value.toString().trim());
      }
      catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }
}
