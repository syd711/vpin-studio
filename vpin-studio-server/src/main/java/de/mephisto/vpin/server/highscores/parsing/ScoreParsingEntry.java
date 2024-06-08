package de.mephisto.vpin.server.highscores.parsing;

public class ScoreParsingEntry {

  private String initials = "???";
  private int pos;
  private long score = 0;

  public String getInitials() {
    return initials;
  }

  public void setInitials(String initials) {
    this.initials = initials;
  }

  public int getPos() {
    return pos;
  }

  public void setPos(int pos) {
    this.pos = pos;
  }

  public long getScore() {
    return score;
  }

  public void setScore(long score) {
    this.score = score;
  }

  @Override
  public String toString() {
    return "#" + getPos() + " " + getInitials() + "   " + getScore();
  }
}
