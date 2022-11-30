package de.mephisto.vpin.restclient.representations;

import java.util.Date;
import java.util.List;

public class ScoreSummaryRepresentation {

  private Date createdAt;

  private String raw;

  public String getRaw() {
    return raw;
  }

  public void setRaw(String raw) {
    this.raw = raw;
  }

  private List<ScoreRepresentation> scores;

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public List<ScoreRepresentation> getScores() {
    return scores;
  }

  public void setScores(List<ScoreRepresentation> scores) {
    this.scores = scores;
  }
}
