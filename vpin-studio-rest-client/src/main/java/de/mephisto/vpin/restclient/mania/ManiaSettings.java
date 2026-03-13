package de.mephisto.vpin.restclient.mania;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

public class ManiaSettings extends JsonSettings {
  private String apiKey;
  private String cabinetUuid;

  private boolean submitAllScores = true;
  private boolean submitRatings = true;
  private boolean submitPlayed = true;
  private boolean submitTables = true;
  private boolean showOnlineStatus = true;
  private boolean showActiveGameStatus = true;

  public String getCabinetUuid() {
    return cabinetUuid;
  }

  public void setCabinetUuid(String cabinetUuid) {
    this.cabinetUuid = cabinetUuid;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

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

  public boolean isSubmitAllScores() {
    return submitAllScores;
  }

  public void setSubmitAllScores(boolean submitAllScores) {
    this.submitAllScores = submitAllScores;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.MANIA_SETTINGS;
  }
}
