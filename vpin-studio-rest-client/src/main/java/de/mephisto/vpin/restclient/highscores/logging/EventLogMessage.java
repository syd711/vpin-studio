package de.mephisto.vpin.restclient.highscores.logging;

import de.mephisto.vpin.restclient.util.DateUtil;

import java.util.Date;
import java.util.Objects;

public class EventLogMessage {
  public enum Severity {
    INFO, WARN, ERROR
  }

  private Date date = new Date();
  private String message;
  private Severity severity;

  public void setDate(Date date) {
    this.date = date;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public void setSeverity(Severity severity) {
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
    return DateUtil.formatDateTime(this.date) + " [" + this.severity.name() + "] " + message;
  }
}
