package de.mephisto.vpin.server.highscores;

import org.apache.commons.lang3.StringUtils;

public class Score {
  private String userInitials = "???";
  private String score;
  private int position;

  public Score(String userInitials, String score, int position) {
    this.score = score;
    this.position = position;
    if (userInitials != null) {
      this.userInitials = userInitials;
    }
  }

  public String getUserInitials() {
    while (userInitials.length() < 3) {
      userInitials += " ";
    }
    return userInitials;
  }

  public void setUserInitials(String userInitials) {
    if (!StringUtils.isEmpty(userInitials.trim())) {
      this.userInitials = userInitials;
    }
  }

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public String getScore() {
    return score;
  }

  public void setScore(String score) {
    this.score = score;
  }

  @Override
  public String toString() {
    return this.getPosition() + ". " + this.getUserInitials() + "   " + this.getScore();
  }
}
