package de.mephisto.vpin.server.highscores.logging;

import java.util.ArrayList;
import java.util.List;

public class HighscoreEventLog {
  private int gameId;

  private List<String> log = new ArrayList<>();

  private List<String> iScoredLog = new ArrayList<>();

  private List<String> discordLog = new ArrayList<>();

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public List<String> getLog() {
    return log;
  }

  public void setLog(List<String> log) {
    this.log = log;
  }

  public List<String> getiScoredLog() {
    return iScoredLog;
  }

  public void setiScoredLog(List<String> iScoredLog) {
    this.iScoredLog = iScoredLog;
  }

  public List<String> getDiscordLog() {
    return discordLog;
  }

  public void setDiscordLog(List<String> discordLog) {
    this.discordLog = discordLog;
  }
}
