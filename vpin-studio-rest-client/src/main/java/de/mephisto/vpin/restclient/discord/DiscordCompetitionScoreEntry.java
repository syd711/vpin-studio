package de.mephisto.vpin.restclient.discord;

public class DiscordCompetitionScoreEntry {
  private int position;
  private String initials;
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

  public String getScore() {
    return score;
  }

  public void setScore(String score) {
    this.score = score;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DiscordCompetitionScoreEntry)) return false;

    DiscordCompetitionScoreEntry that = (DiscordCompetitionScoreEntry) o;

    if (position != that.position) return false;
    if (!initials.equals(that.initials)) return false;
    return score.equals(that.score);
  }

  @Override
  public int hashCode() {
    int result = position;
    result = 31 * result + initials.hashCode();
    result = 31 * result + score.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return position + "# " + getInitials() + "   " + getScore();
  }
}