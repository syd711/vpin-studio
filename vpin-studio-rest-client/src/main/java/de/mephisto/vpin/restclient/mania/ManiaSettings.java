package de.mephisto.vpin.restclient.mania;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

import java.util.ArrayList;
import java.util.List;

public class ManiaSettings extends JsonSettings {
  private boolean enabled;
  private boolean tournamentsEnabled = false;
  private String defaultDiscordLink;
  private String defaultDashboardUrl;
  private String defaultDescription;
  private String defaultWebsite;

  private boolean submitAllScores = true;
  private boolean submitRatings = true;
  private boolean submitPlayed = true;
  private boolean submitTables = true;
  private boolean showOnlineStatus = true;
  private boolean showActiveGameStatus = true;

  public boolean isSubmitTables() {
    return submitTables;
  }

  public void setSubmitTables(boolean submitTables) {
    this.submitTables = submitTables;
  }

  public boolean isSubmitRatings() {
    return submitRatings;
  }

  public void setSubmitRatings(boolean submitRatings) {
    this.submitRatings = submitRatings;
  }

  public boolean isSubmitPlayed() {
    return submitPlayed;
  }

  public void setSubmitPlayed(boolean submitPlayed) {
    this.submitPlayed = submitPlayed;
  }

  public boolean isShowOnlineStatus() {
    return showOnlineStatus;
  }

  public void setShowOnlineStatus(boolean showOnlineStatus) {
    this.showOnlineStatus = showOnlineStatus;
  }

  public boolean isShowActiveGameStatus() {
    return showActiveGameStatus;
  }

  public void setShowActiveGameStatus(boolean showActiveGameStatus) {
    this.showActiveGameStatus = showActiveGameStatus;
  }

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

  @Override
  public String getSettingsName() {
    return PreferenceNames.MANIA_SETTINGS;
  }
}
