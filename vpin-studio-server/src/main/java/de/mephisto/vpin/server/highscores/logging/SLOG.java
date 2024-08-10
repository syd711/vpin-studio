package de.mephisto.vpin.server.highscores.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * The global static score logger.
 * Since the server must process all score request sequentially a simple static implementation does the trick.
 */
public class SLOG {

  private HighscoreEventLog eventLog;

  private final Map<Integer,HighscoreEventLog> logs = new HashMap<>();

  private static SLOG instance = new SLOG();

  public static void initLog(int gameId) {
    instance.eventLog = new HighscoreEventLog();
    instance.eventLog.setGameId(gameId);
  }

  public static void error(String message) {

  }

  public static void warn(String message) {

  }

  public static void info(String message) {

  }

  public static void finalizeEventLog() {

  }
}
