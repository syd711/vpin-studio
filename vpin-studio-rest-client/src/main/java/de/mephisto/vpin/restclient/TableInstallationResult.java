package de.mephisto.vpin.restclient;

public class TableInstallationResult {
  private int gameId;
  private String error;

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
