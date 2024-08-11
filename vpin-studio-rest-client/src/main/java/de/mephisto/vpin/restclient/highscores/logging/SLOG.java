package de.mephisto.vpin.restclient.highscores.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * The global static score logger.
 * Since the server must process all score request sequentially a simple static implementation does the trick.
 */
public class SLOG {
  private HighscoreEventLog eventLog;

  private final Map<Integer, HighscoreEventLog> logs = new HashMap<>();

  private static final SLOG instance = new SLOG();

  public static HighscoreEventLog getLog(int gameId) {
    if (instance.logs.containsKey(gameId)) {
      return instance.logs.get(gameId);
    }
    return null;
  }

  public static void initLog(int gameId) {
    instance.eventLog = new HighscoreEventLog();
    instance.eventLog.setGameId(gameId);
    instance.logs.put(instance.eventLog.getGameId(), instance.eventLog);
  }

  public static void error(String message) {
    if (instance.eventLog != null) {
      instance.log(EventLogMessage.Severity.ERROR, message);
    }
  }

  public static void warn(String message) {
    if (instance.eventLog != null) {
      instance.log(EventLogMessage.Severity.WARN, message);
    }
  }

  public static void info(String message) {
    if (instance.eventLog != null) {
      instance.log(EventLogMessage.Severity.INFO, message);
    }
  }

  private void log(EventLogMessage.Severity severity, String message) {
    EventLogMessage msg = new EventLogMessage(severity, message);
    eventLog.getLog().add(msg);
  }

  public static void finalizeEventLog() {
    instance.eventLog = null;
  }
}
