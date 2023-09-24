package de.mephisto.vpin.restclient.highscores;

import java.util.Date;
import java.util.List;

public class ScoreSummaryRepresentation {

  private Date createdAt;

  private String raw;

  private HighscoreMetadataRepresentation metadata;

  public HighscoreMetadataRepresentation getMetadata() {
    return metadata;
  }

  public void setMetadata(HighscoreMetadataRepresentation metadata) {
    this.metadata = metadata;
  }

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
