package de.mephisto.vpin.server.achievements.rules;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Field classification helpers of the Achievement Watcher (excluded fields, noise, milestones, tally patterns).
 */
public final class AuditFieldRules {

  private AuditFieldRules() {
  }

  private static final Set<String> EXCLUDED_FIELDS_LC = Set.of(
      "last game start", "last printout", "last replay", "champion reset", "clock last set", "coins cleared",
      "factory setting", "recent paid cred", "recent serv. cred", "burn-in time", "totals cleared", "audits cleared",
      "last serv. cred");

  public static final Pattern NOISE_REGEX = Pattern.compile(
      "(minutes on|play time|recent|total .*slot|paid cred|serv|factory|reset|cleared|burn|clock|coins|h\\.s\\.t\\.d)",
      Pattern.CASE_INSENSITIVE);

  /**
   * Fields the Watcher ignores when computing session deltas.
   */
  public static final Set<String> DELTA_IGNORE = Set.of("current_ball", "game over", "tilted", "credits", "player_count",
      "1 player games", "2 player games", "3 player games", "4 player games");

  /**
   * Keyword patterns used to sum ROM specific labels into the canonical fields of the global tally rules.
   */
  public static final Map<String, List<List<String>>> TALLY_PATTERNS = Map.ofEntries(
      Map.entry("Ball Saves", List.of(List.of("ball save"), List.of("ball saver"))),
      Map.entry("Ramps Made", List.of(List.of("ramp"))),
      Map.entry("Jackpots", List.of(List.of("jackpot"), List.of("jckpot"))),
      Map.entry("Total Multiballs", List.of(List.of("multiball"), List.of("multi-ball"))),
      Map.entry("Loops", List.of(List.of("loop"))),
      Map.entry("Combos", List.of(List.of("combo"))),
      Map.entry("Extra Balls", List.of(List.of("extra ball"))),
      Map.entry("Games Started", List.of(List.of("games started"), List.of("games played"))),
      Map.entry("Balls Played", List.of(List.of("balls played"), List.of("ball count"), List.of("total balls"))),
      Map.entry("MINUTES ON", List.of(List.of("minutes on"), List.of("minute")))
  );

  public static boolean isExcludedField(@Nullable String label) {
    String ll = label == null ? "" : label.trim().toLowerCase();
    return EXCLUDED_FIELDS_LC.contains(ll)
        || ll.contains("reset")
        || ll.contains("cleared")
        || ll.contains("factory")
        || ll.contains("timestamp")
        || ll.contains("game time")
        || (ll.contains("last") && (ll.contains("printout") || ll.contains("replay")))
        || (ll.contains("last") && ll.contains("game"));
  }

  public static boolean isNoise(@Nullable String label) {
    return label != null && NOISE_REGEX.matcher(label).find();
  }

  public static boolean isPlausibleCounter(@Nullable String label) {
    if (label == null || label.isEmpty()) {
      return false;
    }
    String l = label.toLowerCase();
    for (String k : List.of("games", "balls", "ramp", "bumper", "spinner", "extra", "bonus", "hits", "made", "served",
        "targets", "loops", "lane", "kicks", "multiball", "jackpot", "mode")) {
      if (l.contains(k)) {
        return true;
      }
    }
    return false;
  }

  public static List<Integer> sessionMilestones(@Nullable String fieldLabel) {
    String f = fieldLabel == null ? "" : fieldLabel.toLowerCase();
    if (f.contains("extra ball")) return List.of(3, 5);
    if (f.contains("ball save")) return List.of(3, 5, 10);
    if (f.contains("jackpot")) return List.of(1, 3, 5, 10, 15);
    if (f.contains("multiball")) return List.of(1, 3, 5);
    if (f.contains("ramp")) return List.of(5, 10, 15, 20, 25);
    if (f.contains("loop") || f.contains("orbit")) return List.of(3, 5, 10, 15);
    if (f.contains("spinner")) return List.of(10, 20, 30, 50);
    if (f.contains("target")) return List.of(10, 20, 30, 50);
    if (f.contains("mode")) return List.of(1, 3, 5, 10);
    return List.of(1, 3, 5, 10, 15, 20, 25, 30);
  }

  public static int sessionCap(@Nullable String label) {
    String l = label == null ? "" : label.toLowerCase();
    if (l.contains("extra ball")) return 5;
    if (l.contains("ball save")) return 10;
    if (l.contains("jackpot")) return 10;
    if (l.contains("multiball")) return 3;
    if (l.contains("ramp")) return 20;
    if (l.contains("loop") || l.contains("orbit")) return 10;
    if (l.contains("spinner")) return 30;
    if (l.contains("target")) return 30;
    if (l.contains("mode")) return 5;
    return 15;
  }

  /**
   * A player field like "P1 Score", the Watcher skips these for session deltas.
   */
  public static boolean isPlayerField(@Nullable String label) {
    return label != null && label.matches("^P\\d+ .*");
  }
}
