package de.mephisto.vpin.restclient.recorder;

public class RecordingScreenOptions {
  private String displayName;
  private int recordingDuration;
  private int initialDelay;
  private RecordMode recordMode;
  private boolean enabled = true;
  private boolean fps60 = false;

  public boolean isFps60() {
    return fps60;
  }

  public void setFps60(boolean fps60) {
    this.fps60 = fps60;
  }

  public int getInitialDelay() {
    return initialDelay;
  }

  public void setInitialDelay(int initialDelay) {
    this.initialDelay = initialDelay;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public int getRecordingDuration() {
    return recordingDuration;
  }

  public void setRecordingDuration(int recordingDuration) {
    this.recordingDuration = recordingDuration;
  }

  public RecordMode getRecordMode() {
    return recordMode;
  }

  public void setRecordMode(RecordMode recordMode) {
    this.recordMode = recordMode;
  }
}