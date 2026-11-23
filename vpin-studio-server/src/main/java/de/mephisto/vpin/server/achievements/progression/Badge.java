package de.mephisto.vpin.server.achievements.progression;

import org.jspecify.annotations.Nullable;

/**
 * The 31 badges of the Achievement Watcher (ids, names and descriptions unchanged for migration compatibility).
 */
public enum Badge {
  first_steps("First Steps", "Unlock your very first achievement", Category.Milestones),
  getting_started("Getting Started", "Unlock 5 unique achievements", Category.Milestones),
  deca("Deca", "Unlock 10 unique achievements", Category.Milestones),
  half_century("Half Century", "Unlock 50 unique achievements", Category.Milestones),
  century("Century", "Unlock 100 unique achievements", Category.Milestones),
  hoarder("Hoarder", "Unlock 500 unique achievements", Category.Milestones),
  thousandaire("Thousandaire", "Unlock 1000 unique achievements", Category.Milestones),
  first_star("First Star", "Reach Prestige 1", Category.Prestige),
  two_stars("Rising Star", "Reach Prestige 2", Category.Prestige),
  three_stars("Superstar", "Reach Prestige 3", Category.Prestige),
  four_stars("Elite Star", "Reach Prestige 4", Category.Prestige),
  five_stars("Maximum Prestige", "Reach Prestige 5 — Fully Maxed", Category.Prestige),
  explorer("Explorer", "Play 10 different tables", Category.Exploration),
  globetrotter("Globetrotter", "Play tables from 5 different manufacturers", Category.Exploration),
  bally_fan("Bally Fan", "Play 5 different Bally tables", Category.Exploration),
  williams_fan("Williams Fan", "Play 5 different Williams tables", Category.Exploration),
  stern_fan("Stern Fan", "Play 5 different Stern tables", Category.Exploration),
  gottlieb_fan("Gottlieb Fan", "Play 5 different Gottlieb tables", Category.Exploration),
  dedicated("Dedicated", "Accumulate 10 hours of total playtime", Category.Playtime),
  marathon("Marathon", "Accumulate 50 hours of total playtime", Category.Playtime),
  addict("Addict", "Accumulate 100 hours of total playtime", Category.Playtime),
  long_session("Endurance", "Play a single session for 60+ minutes", Category.Playtime),
  hot_streak("Hot Streak", "Unlock 5 achievements in a single session", Category.Special),
  night_owl("Night Owl", "Start a session after midnight (00:00–05:00)", Category.Special),
  speed_demon("Speed Demon", "Unlock 3 achievements within 5 minutes", Category.Special),
  rare_finder("Rare Finder", "Unlock a Rare achievement", Category.Rarity),
  epic_hunter("Epic Hunter", "Unlock an Epic achievement", Category.Rarity),
  legendary_hunter("Legendary Hunter", "Unlock a Legendary achievement", Category.Rarity),
  cloud_pioneer("Cloud Pioneer", "Complete your first cloud upload", Category.CloudAndLevel),
  level_5("Level 5", "Reach Player Level 5", Category.CloudAndLevel),
  level_10("Level 10", "Reach Player Level 10", Category.CloudAndLevel);

  public enum Category {
    Milestones("Milestones"), Prestige("Prestige"), Exploration("Exploration"), Playtime("Playtime"),
    Special("Special"), Rarity("Rarity"), CloudAndLevel("Cloud & Level");

    private final String label;

    Category(String label) {
      this.label = label;
    }

    public String getLabel() {
      return label;
    }
  }

  private final String displayName;
  private final String description;
  private final Category category;

  Badge(String displayName, String description, Category category) {
    this.displayName = displayName;
    this.description = description;
    this.category = category;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getDescription() {
    return description;
  }

  public Category getCategory() {
    return category;
  }

  @Nullable
  public static Badge byId(@Nullable String id) {
    if (id == null) {
      return null;
    }
    for (Badge b : values()) {
      if (b.name().equals(id)) {
        return b;
      }
    }
    return null;
  }
}
