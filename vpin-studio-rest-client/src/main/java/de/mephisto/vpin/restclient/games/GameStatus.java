package de.mephisto.vpin.restclient.games;

import java.util.Date;

public class GameStatus {
  private int gameId = -1;
  private Date started = null;

  public boolean isActive() {
    return gameId > 0;
  }

  public Date getStarted() {
    return started;
  }

  public void setStarted(Date started) {
    this.started = started;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }
}
