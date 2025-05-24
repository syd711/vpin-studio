package de.mephisto.vpin.restclient.games;

import java.util.Date;

public class GameStatus {
  private int gameId = -1;
  private int lastActiveId = -1;
  private Date started = null;
  private Date pauseTime;
  private long pauseDuration = 0;

  public void startPause() {
    this.pauseTime = new Date();
  }

  public void finishPause() {
    if (this.pauseTime != null) {
      this.pauseDuration += (System.currentTimeMillis() - this.pauseTime.getTime());
    }
    this.pauseTime = null;
  }

  public long getPauseDurationMs() {
    return pauseDuration;
  }

  public int getLastActiveId() {
    return lastActiveId;
  }

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
    if (gameId == -1) {
      this.pauseDuration = 0;
    }
    if (gameId == -1 && this.gameId != -1) {
      this.lastActiveId = this.gameId;
    }
    if (gameId != -1) {
      this.lastActiveId = -1;
    }
    this.gameId = gameId;
  }
}
