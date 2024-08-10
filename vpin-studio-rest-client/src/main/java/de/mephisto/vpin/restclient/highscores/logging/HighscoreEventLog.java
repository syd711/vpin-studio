package de.mephisto.vpin.restclient.highscores.logging;

import java.util.ArrayList;
import java.util.List;

public class HighscoreEventLog {
  private int gameId;

  private List<EventLogMessage> log = new ArrayList<>();

  private List<EventLogMessage> iScoredLog = new ArrayList<>();

  private List<EventLogMessage> discordLog = new ArrayList<>();

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

  public List<EventLogMessage> getiScoredLog() {
    return iScoredLog;
  }

  public void setiScoredLog(List<EventLogMessage> iScoredLog) {
    this.iScoredLog = iScoredLog;
  }

  public List<EventLogMessage> getDiscordLog() {
    return discordLog;
  }

  public void setDiscordLog(List<EventLogMessage> discordLog) {
    this.discordLog = discordLog;
  }
}
