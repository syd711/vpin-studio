package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.server.highscores.Score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ScoreSummary {
  private String raw;
  private Date createdAt;
  private List<Score> scores;

  public ScoreSummary(List<Score> scores, Date createdAt) {
    this.scores = scores;
    this.createdAt = createdAt;
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

  public void mergeExternalScores(List<Score> externalScores) {
    for (Score externalScore : externalScores) {
      if (!contains(externalScore)) {
        this.scores.add(externalScore);
      }
    }
    Collections.sort(scores, (o1, o2) -> Long.compare(o2.getScore(), o1.getScore()));
    for (int i = 1; i <= scores.size(); i++) {
      Score score = scores.get(i - 1);
      score.setPosition(i);
    }
  }

  public List<Score> cloneEmptyScores() {
    List<Score> emptyClone = new ArrayList<>();
    for (Score score : scores) {
      emptyClone.add(score.cloneEmpty());
    }
    return emptyClone;
  }
}
