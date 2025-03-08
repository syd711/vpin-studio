package de.mephisto.vpin.connectors.iscored.models;

import de.mephisto.vpin.connectors.iscored.Score;

import java.util.ArrayList;
import java.util.List;

public class GameModel {
  private int gameID;
  private String gameName;
  private String hidden;
  private List<String> tags;
  private List<Score> scores = new ArrayList<>();

  public List<Score> getScores() {
    return scores;
  }

  public void setScores(List<Score> scores) {
    this.scores = scores;
  }

  public String getHidden() {
    return hidden;
  }

  public void setHidden(String hidden) {
    this.hidden = hidden;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public int getGameID() {
    return gameID;
  }

  public void setGameID(int gameID) {
    this.gameID = gameID;
  }

  public String getGameName() {
    return gameName;
  }

  public void setGameName(String gameName) {
    this.gameName = gameName;
  }
}
