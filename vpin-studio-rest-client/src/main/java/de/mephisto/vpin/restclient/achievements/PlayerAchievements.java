package de.mephisto.vpin.restclient.achievements;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Achievements overview of a cabinet player.
 */
public class PlayerAchievements {
  public long playerId;
  public String name;
  public String initials;
  public String maniaAccountUuid;
  public boolean tracked;
  public LevelInfo level = new LevelInfo();
  public List<LevelDefinition> levels = new ArrayList<>();
  public int unlockedCount;
  public int unlockedThisWeek;
  public int badgesEarned;
  public int badgesTotal;
  public String selectedBadge;
  public List<AchievementInfo> recentUnlocks = new ArrayList<>();
  public List<AchievementInfo> closest = new ArrayList<>();
  public List<BadgeInfo> badges = new ArrayList<>();
  public int pendingUploads;
  public Date lastSync;
  public String migratedFirebasePid;
}
