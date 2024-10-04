package de.mephisto.vpin.restclient.recorder;

public class RecordingScreenOptions {
  private String displayName;
  private int recordingDuration;
  private RecordMode recordMode;

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
