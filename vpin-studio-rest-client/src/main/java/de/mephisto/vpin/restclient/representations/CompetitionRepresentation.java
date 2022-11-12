package de.mephisto.vpin.restclient.representations;

import java.util.Date;
import java.util.List;

public class CompetitionRepresentation {
  private Long id;

  private String name;

  private int gameId;

  private String badge;

  private String type;

  private Date startDate;

  private Date endDate;

  private boolean customizeMedia;

  private List<HighscoreRepresentation> highscores;



  public List<HighscoreRepresentation> getHighscores() {
    return highscores;
  }

  public void setHighscores(List<HighscoreRepresentation> highscores) {
    this.highscores = highscores;
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    CompetitionRepresentation that = (CompetitionRepresentation) o;

    return id != null && that.id != null && id.equals(that.id);
  }
}
