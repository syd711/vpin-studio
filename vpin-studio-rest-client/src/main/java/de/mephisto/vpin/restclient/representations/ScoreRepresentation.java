package de.mephisto.vpin.restclient.representations;

public class ScoreRepresentation {
  private String userInitials;
  private String score;
  private int position;

  public String getUserInitials() {
    return userInitials;
  }

  public void setUserInitials(String userInitials) {
    this.userInitials = userInitials;
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
}
