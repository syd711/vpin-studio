package de.mephisto.vpin.connectors.iscored;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Settings {
  private String roomName;

  private String gameOrder;

  private String publicScoreEntryEnabled;

  private String publicScoresEnabled;

  private String adminApproval;

  private String datesEnabled;

  private String longNamesEnabled;

  private String tournamentColumn;

  private String apiReadAccess;

  private String apiWriteAccess;

  public String getApiWriteAccess() {
    return apiWriteAccess;
  }

  public void setApiWriteAccess(String apiWriteAccess) {
    this.apiWriteAccess = apiWriteAccess;
  }

  public String getApiReadAccess() {
    return apiReadAccess;
  }

  public void setApiReadAccess(String apiReadAccess) {
    this.apiReadAccess = apiReadAccess;
  }

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

  public String getDatesEnabled() {
    return datesEnabled;
  }

  public void setDatesEnabled(String datesEnabled) {
    this.datesEnabled = datesEnabled;
  }

  public String getPublicScoresEnabled() {
    return publicScoresEnabled;
  }

  public void setPublicScoresEnabled(String publicScoresEnabled) {
    this.publicScoresEnabled = publicScoresEnabled;
  }

  public String getTournamentColumn() {
    return tournamentColumn;
  }

  public void setTournamentColumn(String tournamentColumn) {
    this.tournamentColumn = tournamentColumn;
  }

  @JsonIgnore//do not use original name!
  public boolean isApiReadingEnabled() {
    if (this.apiReadAccess != null && (this.apiReadAccess.equalsIgnoreCase("true") || this.apiReadAccess.equalsIgnoreCase("false"))) {
      return Boolean.parseBoolean(this.apiReadAccess.toLowerCase());
    }
    return false;
  }

  @JsonIgnore//do not use original name!
  public boolean isApiWritingEnabled() {
    if (this.apiWriteAccess != null && (this.apiWriteAccess.equalsIgnoreCase("true") || this.apiWriteAccess.equalsIgnoreCase("false"))) {
      return Boolean.parseBoolean(this.apiWriteAccess.toLowerCase());
    }
    return false;
  }

  @JsonIgnore//do not use original name!
  public boolean isPublicScoresReadingEnabled() {
    if (this.publicScoresEnabled != null && (this.publicScoresEnabled.equalsIgnoreCase("true") || this.publicScoresEnabled.equalsIgnoreCase("false"))) {
      return Boolean.parseBoolean(this.publicScoresEnabled.toLowerCase());
    }
    return false;
  }
  @JsonIgnore//do not use original name!
  public boolean isCompetitionColumnEnabled() {
    if (this.tournamentColumn != null && (this.tournamentColumn.equalsIgnoreCase("true") || this.tournamentColumn.equalsIgnoreCase("false"))) {
      return Boolean.parseBoolean(this.tournamentColumn.toLowerCase());
    }
    return false;
  }

  @JsonIgnore//do not use original name!
  public boolean isDateFieldEnabled() {
    if (this.datesEnabled != null && (this.datesEnabled.equalsIgnoreCase("true") || this.datesEnabled.equalsIgnoreCase("false"))) {
      return Boolean.parseBoolean(this.datesEnabled.toLowerCase());
    }
    return false;
  }

  @JsonIgnore//do not use original name!
  public boolean isPublicScoreEnteringEnabled() {
    if (this.publicScoreEntryEnabled != null && (this.publicScoreEntryEnabled.equalsIgnoreCase("true") || this.publicScoreEntryEnabled.equalsIgnoreCase("false"))) {
      return Boolean.parseBoolean(this.publicScoreEntryEnabled.toLowerCase());
    }
    return false;
  }

  @JsonIgnore//do not use original name!
  public boolean isAdminApprovalEnabled() {
    if (this.adminApproval != null && (this.adminApproval.equalsIgnoreCase("true") || this.adminApproval.equalsIgnoreCase("false"))) {
      return Boolean.parseBoolean(this.adminApproval.toLowerCase());
    }
    return false;
  }

  @JsonIgnore //do not use original name!
  public boolean isLongNameInputEnabled() {
    if (this.longNamesEnabled != null && (this.longNamesEnabled.equalsIgnoreCase("true") || this.longNamesEnabled.equalsIgnoreCase("false"))) {
      return Boolean.parseBoolean(this.longNamesEnabled.toLowerCase());
    }
    return false;
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
