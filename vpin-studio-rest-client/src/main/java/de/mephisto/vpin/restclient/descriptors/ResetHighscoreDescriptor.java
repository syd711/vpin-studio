package de.mephisto.vpin.restclient.descriptors;

public class ResetHighscoreDescriptor {
  private int gameId;
  private boolean deleteHistory;

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }
}
