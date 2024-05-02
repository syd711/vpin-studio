package de.mephisto.vpin.restclient.tournaments;

public class TournamentMetaData {
  private String badge;
  private long tournamentId;
  private boolean resetHighscores;

  public String getBadge() {
    return badge;
  }

  public void setBadge(String badge) {
    this.badge = badge;
  }

  public long getTournamentId() {
    return tournamentId;
  }

  public void setTournamentId(long tournamentId) {
    this.tournamentId = tournamentId;
  }

  public boolean isResetHighscores() {
    return resetHighscores;
  }

  public void setResetHighscores(boolean resetHighscores) {
    this.resetHighscores = resetHighscores;
  }
}
