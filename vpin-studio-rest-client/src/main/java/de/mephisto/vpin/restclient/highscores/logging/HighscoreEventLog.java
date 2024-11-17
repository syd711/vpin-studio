package de.mephisto.vpin.restclient.highscores.logging;

import de.mephisto.vpin.restclient.JsonSettings;

import java.util.ArrayList;
import java.util.List;

public class HighscoreEventLog extends JsonSettings {
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

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (EventLogMessage eventLogMessage : log) {
      builder.append(eventLogMessage.toString());
      builder.append("\n");
    }
    return builder.toString();
  }

  @Override
  public String getSettingsName() {
    return "EventLog";
  }
}
