package de.mephisto.vpin.connectors.iscored;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Settings {
  private String roomName;

  private String gameOrder;

  private String publicScoreEntryEnabled;

  private String adminApproval;

  private String longNamesEnabled;

  public String getLongNamesEnabled() {
    return longNamesEnabled;
  }

  public void setLongNamesEnabled(String longNamesEnabled) {
    this.longNamesEnabled = longNamesEnabled;
  }

  public String getRoomName() {
    return roomName;
  }

  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }

  public String getGameOrder() {
    return gameOrder;
  }

  public void setGameOrder(String gameOrder) {
    this.gameOrder = gameOrder;
  }

  @JsonIgnore
  public boolean isPublicScoresEnabled() {
    return Boolean.parseBoolean(this.publicScoreEntryEnabled.toLowerCase());
  }

  @JsonIgnore //do not use original name!
  public boolean isLongNameInputEnabled() {
    return Boolean.parseBoolean(this.longNamesEnabled.toLowerCase());
  }

  public String getPublicScoreEntryEnabled() {
    return publicScoreEntryEnabled;
  }

  public void setPublicScoreEntryEnabled(String publicScoreEntryEnabled) {
    this.publicScoreEntryEnabled = publicScoreEntryEnabled;
  }

  public String getAdminApproval() {
    return adminApproval;
  }

  public void setAdminApproval(String adminApproval) {
    this.adminApproval = adminApproval;
  }
}
