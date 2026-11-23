package de.mephisto.vpin.restclient.achievements;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

/**
 * An achievement (rule) and its state for a player.
 */
public class AchievementInfo {
  public String title;
  /** the title without the "rom – " prefix */
  public String displayTitle;
  public String rom;
  /** table name for display, may be null for global achievements */
  public String tableName;
  /** "global" or "rom" */
  public String scope;
  public boolean unlocked;
  public Date unlockedAt;
  public String condition;
  /** Common, Uncommon, Rare, Epic, Legendary or null if unknown */
  public String rarity;
  public double rarityPct;
  public long progressCurrent;
  public long progressTarget;
  /** true for unlocked titles that are not part of the current rule set (e.g. migrated) */
  public boolean legacy;

  @JsonIgnore
  public double getProgressPct() {
    if (progressTarget <= 0) {
      return unlocked ? 100 : 0;
    }
    return Math.min(100d, progressCurrent * 100d / progressTarget);
  }
}
