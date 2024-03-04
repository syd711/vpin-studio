package de.mephisto.vpin.server.highscores.vpreg;

public class VPRegScoreEntry {

  private String initials;
  private int pos;
  private String base64Name;
  private String base64Score;
  private long score;

  public String getBase64Name() {
    return base64Name;
  }

  public void setBase64Name(String base64Name) {
    this.base64Name = base64Name;
  }

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

  public String getBase64Score() {
    return base64Score;
  }

  public void setBase64Score(String base64Score) {
    this.base64Score = base64Score;
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
