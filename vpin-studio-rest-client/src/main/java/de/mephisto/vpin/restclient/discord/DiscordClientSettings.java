package de.mephisto.vpin.restclient.discord;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

/**
 * Settings for the locally installed Discord client.
 */
public class DiscordClientSettings extends JsonSettings {
  private boolean launchOnStartup = false;
  private boolean statusUpdateEnabled = false;

  public boolean isLaunchOnStartup() {
    return launchOnStartup;
  }

  public void setLaunchOnStartup(boolean launchOnStartup) {
    this.launchOnStartup = launchOnStartup;
  }

  public boolean isStatusUpdateEnabled() {
    return statusUpdateEnabled;
  }

  public void setStatusUpdateEnabled(boolean statusUpdateEnabled) {
    this.statusUpdateEnabled = statusUpdateEnabled;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.DISCORD_CLIENT_SETTINGS;
  }
}
