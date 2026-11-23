package de.mephisto.vpin.restclient.achievements;

/**
 * Migration of Achievement Watcher data. Source "firebase": Player ID + Recovery Key (claim once, then read).
 * Source "local": a local Watcher installation folder; uploaded to VPin Mania only if the claim with the keys of
 * its cloud_identity.json succeeds, otherwise imported locally only.
 */
public class AchievementsMigrationRequest {
  public static final String SOURCE_FIREBASE = "firebase";
  public static final String SOURCE_LOCAL = "local";

  public long playerId;
  public String source = SOURCE_FIREBASE;
  public String firebasePid;
  public String recoveryKey;
  public String watcherFolder;
  /** true to only read and preview the data, no claim and no import */
  public boolean previewOnly = true;
}
