package de.mephisto.vpin.restclient.representations;

import java.util.ArrayList;
import java.util.List;

public class ScoreListRepresentation {
  private List<ScoreSummaryRepresentation> scores = new ArrayList<>();
  private ScoreSummaryRepresentation latestScore;

  public List<ScoreSummaryRepresentation> getScores() {
    return scores;
  }

  public void setScores(List<ScoreSummaryRepresentation> scores) {
    this.scores = scores;
  }

  public ScoreSummaryRepresentation getLatestScore() {
    return latestScore;
  }

  public void setLatestScore(ScoreSummaryRepresentation latestScore) {
    this.latestScore = latestScore;
  }
}
