package de.mephisto.vpin.restclient;

public class ResetHighscoreDescriptor {
  private int gameId;
  private boolean deleteHistory;

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public boolean isDeleteHistory() {
    return deleteHistory;
  }

  public void setDeleteHistory(boolean deleteHistory) {
    this.deleteHistory = deleteHistory;
  }
}
