package de.mephisto.vpin.restclient.tournaments;

import de.mephisto.vpin.restclient.JsonSettings;

public class TournamentSettings extends JsonSettings {
  private boolean enabled;
  private boolean tournamentsEnabled = false;
  private String defaultDiscordLink;
  private String defaultDashboardUrl;
  private String defaultDescription;
  private String defaultWebsite;
  private boolean submitAllScores = true;

  public boolean isTournamentsEnabled() {
    return tournamentsEnabled;
  }

  public void setTournamentsEnabled(boolean tournamentsEnabled) {
    this.tournamentsEnabled = tournamentsEnabled;
  }

  public boolean isSubmitAllScores() {
    return submitAllScores;
  }

  public void setSubmitAllScores(boolean submitAllScores) {
    this.submitAllScores = submitAllScores;
  }

  public String getDefaultWebsite() {
    return defaultWebsite;
  }

  public void setDefaultWebsite(String defaultWebsite) {
    this.defaultWebsite = defaultWebsite;
  }

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
