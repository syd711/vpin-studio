package de.mephisto.vpin.restclient.alx;

import java.util.Date;

public class TableAlxEntry {
  private int uniqueId;
  private int gameId;
  private Date lastPlayed;
  private int numberOfPlays;
  private int timePlayedSecs;
  private String displayName;
  private int scores;

  public int getScores() {
    return scores;
  }

  public void setScores(int scores) {
    this.scores = scores;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public int getUniqueId() {
    return uniqueId;
  }

  public void setUniqueId(int uniqueId) {
    this.uniqueId = uniqueId;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public Date getLastPlayed() {
    return lastPlayed;
  }

  public void setLastPlayed(Date lastPlayed) {
    this.lastPlayed = lastPlayed;
  }

  public int getNumberOfPlays() {
    return numberOfPlays;
  }

  public void setNumberOfPlays(int numberOfPlays) {
    this.numberOfPlays = numberOfPlays;
  }

  public int getTimePlayedSecs() {
    return timePlayedSecs;
  }

  public void setTimePlayedSecs(int timePlayedSecs) {
    this.timePlayedSecs = timePlayedSecs;
  }
}
