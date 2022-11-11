package de.mephisto.vpin.restclient.representations;

import java.util.Date;

public class CompetitionRepresentation {
  private Long id;

  private String name;

  private int gameId;

  private String type;

  private String badge;

  private Date startDate;

  private Date endDate;

  private boolean customizeMedia;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public boolean isCustomizeMedia() {
    return customizeMedia;
  }

  public void setCustomizeMedia(boolean customizeMedia) {
    this.customizeMedia = customizeMedia;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getBadge() {
    return badge;
  }

  public void setBadge(String badge) {
    this.badge = badge;
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
}
