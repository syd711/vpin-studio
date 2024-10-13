package de.mephisto.vpin.server.recorder;

import org.apache.commons.lang3.time.DurationFormatUtils;

public class RecordingResult {
  private long duration = 0;
  private String fileName;

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

  @Override
  public String toString() {
    return "Recording of \"" + fileName + "\", duration: " + DurationFormatUtils.formatDuration(duration, "HH 'hours', mm 'minutes', ss 'seconds'");
  }
}
