package de.mephisto.vpin.restclient.highscores.logging;

import de.mephisto.vpin.restclient.util.DateUtil;

import java.util.Date;
import java.util.Objects;

public class EventLogMessage {
  public enum Severity {
    INFO, WARN, ERROR
  }

  private final Date date;
  private final String message;
  private final Severity severity;

  public EventLogMessage(Severity severity, String message) {
    this.date = new Date();
    this.message = message;
    this.severity = severity;
  }

  public Date getDate() {
    return date;
  }

  public String getMessage() {
    return message;
  }

  public Severity getSeverity() {
    return severity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    EventLogMessage that = (EventLogMessage) o;
    return Objects.equals(date, that.date) && Objects.equals(message, that.message) && severity == that.severity;
  }

  @Override
  public int hashCode() {
    return Objects.hash(date, message, severity);
  }

  @Override
  public String toString() {
    return DateUtil.formatTimeString(this.date) + " [" + this.severity.name() + "] " + message;
  }
}
