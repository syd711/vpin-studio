package de.mephisto.vpin.restclient.discord;

public class DiscordCompetitionScoreEntry {
  private int position;
  private String initials;
  private double numericScore;
  private String score;

  public int getPosition() {
    return position;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  public String getInitials() {
    return initials;
  }

  public void setInitials(String initials) {
    this.initials = initials;
  }

  public double getNumericScore() {
    return numericScore;
  }

  public void setNumericScore(double numericScore) {
    this.numericScore = numericScore;
  }

  public String getScore() {
    return score;
  }

  public void setScore(String score) {
    this.score = score;
  }
}