package de.mephisto.vpin.restclient.achievements;

import java.util.ArrayList;
import java.util.List;

/**
 * Preview or result of a migration.
 */
public class AchievementsMigrationResult {
  public boolean success;
  public String message;
  public String firebasePid;
  public String watcherPlayerName;
  public int level;
  public String levelName;
  public int prestige;
  public int uniqueAchievements;
  public int romsPlayed;
  public List<String> badges = new ArrayList<>();
  public String selectedBadge;
  /** true if the ownership was verified with Firebase (claim succeeded) */
  public boolean cloudVerified;
  /** true if the data will be/was uploaded to VPin Mania */
  public boolean cloudUpload;
  /** local import only: true if the signature of the local state file is valid */
  public boolean signatureValid;
  public int importedUnlocks;
  public int skippedUnlocks;
  public List<String> warnings = new ArrayList<>();
}
