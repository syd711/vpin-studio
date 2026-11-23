package de.mephisto.vpin.server.achievements.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The part of a player's achievement state the rule evaluation needs. The evaluation updates the tallies and
 * the played ROMs in place.
 */
public class PlayerProgress {
  public Set<String> unlockedTitles = new HashSet<>();
  /** title -> tally of a global rule (progress, and for set rules the number of installed tables) */
  public Map<String, Tally> globalTally = new HashMap<>();
  public Set<String> romsPlayed = new LinkedHashSet<>();

  public static class Tally {
    public long progress;
    public Integer installedCount;

    public Tally() {
    }

    public Tally(long progress, Integer installedCount) {
      this.progress = progress;
      this.installedCount = installedCount;
    }
  }
}
