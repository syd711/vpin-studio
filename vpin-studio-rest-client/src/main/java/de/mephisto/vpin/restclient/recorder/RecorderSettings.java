package de.mephisto.vpin.restclient.recorder;

import de.mephisto.vpin.restclient.JsonSettings;

import java.util.ArrayList;
import java.util.List;

public class RecorderSettings extends JsonSettings {

  private int startDelay;
  private List<RecordingScreenOptions> recordingScreenOptions = new ArrayList<>();

  public List<RecordingScreenOptions> getRecordingScreenOptions() {
    return recordingScreenOptions;
  }

  public void setRecordingScreenOptions(List<RecordingScreenOptions> recordingScreenOptions) {
    this.recordingScreenOptions = recordingScreenOptions;
  }

  public int getStartDelay() {
    return startDelay;
  }

  public void setStartDelay(int startDelay) {
    this.startDelay = startDelay;
  }
}
