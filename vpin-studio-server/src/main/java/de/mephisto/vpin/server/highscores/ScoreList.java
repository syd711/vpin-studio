package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.competitions.ScoreSummary;

import java.util.ArrayList;
import java.util.List;

public class ScoreList {
  private List<ScoreSummary> scores = new ArrayList<>();
  private ScoreSummary latestScore;

  public ScoreSummary getLatestScore() {
    return latestScore;
  }

  public void setLatestScore(ScoreSummary latestScore) {
    this.latestScore = latestScore;
  }

  public List<ScoreSummary> getScores() {
    return scores;
  }

  public void setScores(List<ScoreSummary> scores) {
    this.scores = scores;
  }
}
