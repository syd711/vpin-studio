package de.mephisto.vpin.restclient.games;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

public class GameStatus {
  private int gameId = -1;
  private int lastActiveId = -1;
  private OffsetDateTime started = null;
  private OffsetDateTime pauseTime;
  private long pauseDuration = 0;

  public void startPause() {
    this.pauseTime = OffsetDateTime.now();
  }

  public void finishPause() {
    if (this.pauseTime != null) {
      this.pauseDuration += ChronoUnit.MILLIS.between(this.pauseTime, OffsetDateTime.now());
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

  public OffsetDateTime getStarted() {
    return started;
  }

  public void setStarted(OffsetDateTime started) {
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
