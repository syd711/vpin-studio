package de.mephisto.vpin.connectors.iscored.models;

import de.mephisto.vpin.connectors.iscored.Score;

import java.util.ArrayList;
import java.util.List;

public class GameScoreModel {
  private int gameID;
  private List<Score> scores = new ArrayList<>();

  public int getGameID() {
    return gameID;
  }

  public void setGameID(int gameID) {
    this.gameID = gameID;
  }

  public List<Score> getScores() {
    return scores;
  }

  public void setScores(List<Score> scores) {
    this.scores = scores;
  }
}
