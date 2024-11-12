package de.mephisto.vpin.restclient.recorder;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

import java.util.ArrayList;
import java.util.List;

public class RecorderSettings extends JsonSettings {

  private int startDelay = 2;
  private int refreshInterval = 2;
  private List<RecordingScreenOptions> recordingScreenOptions = new ArrayList<>();

  public List<RecordingScreenOptions> getRecordingScreenOptions() {
    return recordingScreenOptions;
  }

  public void setRecordingScreenOptions(List<RecordingScreenOptions> recordingScreenOptions) {
    this.recordingScreenOptions = recordingScreenOptions;
  }

  public int getRefreshInterval() {
    return refreshInterval;
  }

  public void setRefreshInterval(int refreshInterval) {
    this.refreshInterval = refreshInterval;
  }

  public int getStartDelay() {
    return startDelay;
  }

  public void setStartDelay(int startDelay) {
    this.startDelay = startDelay;
  }

  public RecordingScreenOptions getRecordingScreenOption(RecordingScreen recordingScreen) {
    for (RecordingScreenOptions recordingScreenOption : recordingScreenOptions) {
      if (recordingScreenOption.getDisplayName().equalsIgnoreCase(recordingScreen.getScreen().name())) {
        return recordingScreenOption;
      }
    }
    return null;
  }

  public RecordingScreenOptions getRecordingScreenOption(VPinScreen screen) {
    for (RecordingScreenOptions recordingScreenOption : recordingScreenOptions) {
      if (recordingScreenOption.getDisplayName().equalsIgnoreCase(screen.name())) {
        return recordingScreenOption;
      }
    }
    return null;
  }

  public boolean isEnabled(VPinScreen screen) {
    RecordingScreenOptions option = getRecordingScreenOption(screen);
    return option != null ? option.isEnabled() : false;
  }

  public boolean isEnabled() {
    for (RecordingScreenOptions recordingScreenOption : recordingScreenOptions) {
      if(recordingScreenOption.isEnabled()) {
        return true;
      }
    }
    return false;
  }
}
