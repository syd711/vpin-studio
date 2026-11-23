package de.mephisto.vpin.restclient.achievements;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

/**
 * Settings of the achievements tracking (ported VPX Achievement Watcher).
 * Which players are tracked is configured per player ({@code achievementsEnabled}).
 */
public class AchievementsSettings extends JsonSettings {
  private boolean enabled = true;
  private boolean cloudSyncEnabled = true;
  private int minSessionSeconds = 60;
  /**
   * VPX 10.8.1+: installs the score-server plugin (and a patched PinMAME plugin) for per game evaluation, live
   * unlocks and the detection of multiplayer games.
   */
  private boolean liveTrackingEnabled = false;

  public boolean isLiveTrackingEnabled() {
    return liveTrackingEnabled;
  }

  public void setLiveTrackingEnabled(boolean liveTrackingEnabled) {
    this.liveTrackingEnabled = liveTrackingEnabled;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isCloudSyncEnabled() {
    return cloudSyncEnabled;
  }

  public void setCloudSyncEnabled(boolean cloudSyncEnabled) {
    this.cloudSyncEnabled = cloudSyncEnabled;
  }

  public int getMinSessionSeconds() {
    return minSessionSeconds;
  }

  public void setMinSessionSeconds(int minSessionSeconds) {
    this.minSessionSeconds = minSessionSeconds;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.ACHIEVEMENTS_SETTINGS;
  }
}
