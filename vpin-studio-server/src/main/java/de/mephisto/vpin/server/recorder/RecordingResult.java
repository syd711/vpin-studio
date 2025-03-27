package de.mephisto.vpin.server.recorder;

import org.apache.commons.lang3.time.DurationFormatUtils;

public class RecordingResult {
  private long duration = 0;
  private String fileName;
  private String command;
  private String infoLog;
  private String errorLog;

  public String getInfoLog() {
    return infoLog;
  }

  public void setInfoLog(String infoLog) {
    this.infoLog = infoLog;
  }

  public String getErrorLog() {
    return errorLog;
  }

  public void setErrorLog(String errorLog) {
    this.errorLog = errorLog;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public boolean hasRecorded() {
    return this.duration > 0;
  }

  @Override
  public String toString() {
    return "Recording of \"" + fileName + "\", duration: " + DurationFormatUtils.formatDuration(duration, "HH 'hours', mm 'minutes', ss 'seconds'");
  }
}
