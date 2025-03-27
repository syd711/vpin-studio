package de.mephisto.vpin.restclient.recorder;

public class RecordingScreenOptions {
  private String displayName;
  private int recordingDuration = 10;
  private int initialDelay = 2;
  private RecordingWriteMode recordingWriteMode = RecordingWriteMode.ifMissing;
  private boolean enabled = true;
  private boolean fps60 = false;
  private boolean inGameRecording = false;
  private boolean recordAudio = false;
  private boolean rotated = false;

  private boolean expertSettingsEnabled = false;
  private String customFfmpegCommand = null;

  public boolean isExpertSettingsEnabled() {
    return expertSettingsEnabled;
  }

  public void setExpertSettingsEnabled(boolean expertSettingsEnabled) {
    this.expertSettingsEnabled = expertSettingsEnabled;
  }

  public String getCustomFfmpegCommand() {
    return customFfmpegCommand;
  }

  public void setCustomFfmpegCommand(String customFfmpegCommand) {
    this.customFfmpegCommand = customFfmpegCommand;
  }

  public boolean isRotated() {
    return rotated;
  }

  public void setRotated(boolean rotated) {
    this.rotated = rotated;
  }

  public boolean isRecordAudio() {
    return recordAudio;
  }

  public void setRecordAudio(boolean recordAudio) {
    this.recordAudio = recordAudio;
  }

  public boolean isInGameRecording() {
    return inGameRecording;
  }

  public void setInGameRecording(boolean inGameRecording) {
    this.inGameRecording = inGameRecording;
  }

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

  public RecordingWriteMode getRecordMode() {
    return recordingWriteMode;
  }

  public void setRecordMode(RecordingWriteMode recordingWriteMode) {
    this.recordingWriteMode = recordingWriteMode;
  }
}
