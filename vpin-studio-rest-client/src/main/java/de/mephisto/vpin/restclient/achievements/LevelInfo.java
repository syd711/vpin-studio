package de.mephisto.vpin.restclient.achievements;

/**
 * Player level as computed by the Achievement Watcher ({@code compute_player_level}).
 */
public class LevelInfo {
  public int level = 1;
  public String name = "Rookie";
  /** name of the next level, null at max level */
  public String nextName;
  /** number of unique unlocked achievements */
  public int total;
  public int nextAt;
  public int prevAt;
  public double progressPct;
  public boolean maxLevel;
  /** achievements counted in the current prestige round */
  public int effective;
  public int prestige;
  public String prestigeDisplay = "☆☆☆☆☆";
  public boolean fullyMaxed;
}
