package de.mephisto.vpin.restclient.achievements;

/**
 * Result of a VPin Mania synchronization.
 */
public class AchievementsSyncResult {
  public boolean success;
  public String message;
  public int syncedPlayers;
  public int uploadedUnlocks;
  public int skippedPlayers;
}
