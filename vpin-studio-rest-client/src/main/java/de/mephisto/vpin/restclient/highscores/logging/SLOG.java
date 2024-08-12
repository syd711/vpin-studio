package de.mephisto.vpin.restclient.highscores.logging;

/**
 * The global static score logger.
 * Since the server must process all score request sequentially a simple static implementation does the trick.
 */
public class SLOG {
  private HighscoreEventLog eventLog;

  private static final SLOG instance = new SLOG();

  public static void initLog(int gameId) {
    instance.eventLog = new HighscoreEventLog();
    instance.eventLog.setGameId(gameId);
    info("******** Event Log Start **********");
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
    EventLogMessage msg = new EventLogMessage();
    msg.setMessage(message);
    msg.setSeverity(severity);
    eventLog.getLog().add(msg);
  }

  public static HighscoreEventLog finalizeEventLog() {
    HighscoreEventLog l = instance.eventLog;
    instance.eventLog = null;

    return l;
  }
}
