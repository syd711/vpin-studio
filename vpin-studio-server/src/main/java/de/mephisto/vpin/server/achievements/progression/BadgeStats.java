package de.mephisto.vpin.server.achievements.progression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Statistics needed for the badge evaluation (port of {@code _gather_badge_stats}, with the Watcher's bugs fixed:
 * per session unlock counts, rarity of the player's own unlocks, all session start times, ALX playtime).
 */
public class BadgeStats {
  public int totalAchievements;
  public int level = 1;
  public int prestige;
  public boolean fullyMaxed;
  public Set<String> romsPlayed = new HashSet<>();
  /** manufacturer -> played ROMs */
  public Map<String, Set<String>> manufacturerRoms = new HashMap<>();
  public long totalPlaytimeSec;
  public long maxSessionSec;
  public int maxSessionUnlocks;
  /** most unlocks within any 5 minute window */
  public int maxUnlocksIn5Min;
  public boolean nightOwl;
  public boolean hasRare;
  public boolean hasEpic;
  public boolean hasLegendary;
  public boolean cloudUploadDone;

  public int manufacturerCount(String manufacturer) {
    Set<String> roms = manufacturerRoms.get(manufacturer);
    return roms != null ? roms.size() : 0;
  }
}
