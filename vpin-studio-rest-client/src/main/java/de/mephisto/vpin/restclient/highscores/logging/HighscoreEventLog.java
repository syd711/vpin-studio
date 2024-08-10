package de.mephisto.vpin.restclient.highscores.logging;

import java.util.ArrayList;
import java.util.List;

public class HighscoreEventLog {
  private int gameId;

  private List<EventLogMessage> log = new ArrayList<>();

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public List<EventLogMessage> getLog() {
    return log;
  }

  public void setLog(List<EventLogMessage> log) {
    this.log = log;
  }
}
