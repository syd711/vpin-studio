package de.mephisto.vpin.server.highscores;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Highscore {
  private List<Score> scores = new ArrayList<>();

  private String userInitials = "???";
  private String score;

  private String raw;

  public Highscore(String cmdOutput) {
    this.raw = cmdOutput;
  }

  public List<Score> getScores() {
    return scores;
  }

  public void setScores(List<Score> scores) {
    this.scores = scores;
  }

  public String getUserInitials() {
    return userInitials;
  }

  public void setUserInitials(String userInitials) {
    if (!StringUtils.isEmpty(userInitials.trim())) {
      this.userInitials = userInitials;
    }
  }

  public String getScore() {
    return score;
  }

  public void setScore(String score) {
    this.score = score;
  }

  public String getRaw() {
    return raw;
  }

  @Override
  public String toString() {
    return this.raw;
  }
}
