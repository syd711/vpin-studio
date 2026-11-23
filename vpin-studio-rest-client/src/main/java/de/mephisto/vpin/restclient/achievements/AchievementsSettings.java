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
