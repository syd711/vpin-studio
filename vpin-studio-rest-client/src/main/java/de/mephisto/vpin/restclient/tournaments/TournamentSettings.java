package de.mephisto.vpin.restclient.tournaments;

import de.mephisto.vpin.restclient.JsonSettings;

public class TournamentSettings extends JsonSettings {
  private boolean enabled;
  private String defaultDiscordLink;
  private String defaultDashboardUrl;
  private String defaultDescription;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getDefaultDiscordLink() {
    return defaultDiscordLink;
  }

  public void setDefaultDiscordLink(String defaultDiscordLink) {
    this.defaultDiscordLink = defaultDiscordLink;
  }

  public String getDefaultDashboardUrl() {
    return defaultDashboardUrl;
  }

  public void setDefaultDashboardUrl(String defaultDashboardUrl) {
    this.defaultDashboardUrl = defaultDashboardUrl;
  }

  public String getDefaultDescription() {
    return defaultDescription;
  }

  public void setDefaultDescription(String defaultDescription) {
    this.defaultDescription = defaultDescription;
  }
}
