package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.Score;

import java.util.Date;
import java.util.List;

public class ScoreSummary {
  private String raw;
  private Date createdAt;
  private List<Score> scores;
  private HighscoreMetadata metadata;

  public ScoreSummary(List<Score> scores, Date createdAt) {
    this.scores = scores;
    this.createdAt = createdAt;
  }

  public HighscoreMetadata getMetadata() {
    return metadata;
  }

  public void setMetadata(HighscoreMetadata metadata) {
    this.metadata = metadata;
  }

  public String getRaw() {
    return raw;
  }

  public void setRaw(String raw) {
    this.raw = raw;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
  }

  public List<Score> getScores() {
    return scores;
  }

  public void setScores(List<Score> scores) {
    this.scores = scores;
  }

  public boolean contains(Score newScore) {
    return scores.stream().anyMatch(s -> s.matches(newScore));
  }
}
