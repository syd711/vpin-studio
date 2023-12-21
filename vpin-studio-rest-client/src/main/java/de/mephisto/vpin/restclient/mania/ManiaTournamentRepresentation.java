package de.mephisto.vpin.restclient.mania;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class ManiaTournamentRepresentation {
  private int id;
  private String uuid;
  private String displayName;
  private Date startDate;
  private Date endDate;
  private String ownerUuid;
  private String tournamentMode;
  private String tournamentRuleSet;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public String getOwnerUuid() {
    return ownerUuid;
  }

  public void setOwnerUuid(String ownerUuid) {
    this.ownerUuid = ownerUuid;
  }

  public String getTournamentMode() {
    return tournamentMode;
  }

  public void setTournamentMode(String tournamentMode) {
    this.tournamentMode = tournamentMode;
  }

  public String getTournamentRuleSet() {
    return tournamentRuleSet;
  }

  public void setTournamentRuleSet(String tournamentRuleSet) {
    this.tournamentRuleSet = tournamentRuleSet;
  }
}
