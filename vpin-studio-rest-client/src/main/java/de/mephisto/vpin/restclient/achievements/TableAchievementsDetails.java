package de.mephisto.vpin.restclient.achievements;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Achievements of a table for a player.
 */
public class TableAchievementsDetails {
  public TableAchievements table;
  public List<AchievementInfo> achievements = new ArrayList<>();
  /** rarity tier -> number of unlocked achievements */
  public Map<String, Integer> rarityDistribution = new LinkedHashMap<>();
  public boolean rarityAvailable;
}
