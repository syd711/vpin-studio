package de.mephisto.vpin.server.achievements.progression;

import org.jspecify.annotations.Nullable;

/**
 * Rarity tiers of the Achievement Watcher, based on the share of players (with at least one achievement on the
 * table) that unlocked an achievement.
 */
public enum RarityTier {
  Common(50.0), Uncommon(25.0), Rare(10.0), Epic(5.0), Legendary(0.0);

  private final double threshold;

  RarityTier(double threshold) {
    this.threshold = threshold;
  }

  @Nullable
  public static RarityTier of(int unlockedBy, int totalPlayers) {
    if (totalPlayers <= 0) {
      return null;
    }
    double pct = unlockedBy * 100d / totalPlayers;
    for (RarityTier tier : values()) {
      if (pct >= tier.threshold) {
        return tier;
      }
    }
    return Legendary;
  }

  public static double pct(int unlockedBy, int totalPlayers) {
    return totalPlayers <= 0 ? 0 : Math.round(unlockedBy * 1000d / totalPlayers) / 10d;
  }

  public boolean isAtLeast(RarityTier other) {
    return this.ordinal() >= other.ordinal();
  }
}
