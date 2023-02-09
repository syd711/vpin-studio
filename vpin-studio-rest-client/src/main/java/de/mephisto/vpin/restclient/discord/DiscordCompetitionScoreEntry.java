package de.mephisto.vpin.restclient.discord;

public class DiscordCompetitionScoreEntry {
  private int p;
  private String i;
  private String s;

  public int getP() {
    return p;
  }

  public void setP(int p) {
    this.p = p;
  }

  public String getI() {
    return i;
  }

  public void setI(String i) {
    this.i = i;
  }

  public String getS() {
    return s;
  }

  public void setS(String s) {
    this.s = s;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DiscordCompetitionScoreEntry)) return false;

    DiscordCompetitionScoreEntry that = (DiscordCompetitionScoreEntry) o;

    if (p != that.p) return false;
    if (!i.equals(that.i)) return false;
    return s.equals(that.s);
  }

  @Override
  public int hashCode() {
    int result = p;
    result = 31 * result + i.hashCode();
    result = 31 * result + s.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return p + "# " + getI() + "   " + getS();
  }
}