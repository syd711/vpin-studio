package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

public class PauseMenuSettings extends JsonSettings {
  private boolean useOverlayKey;
  private String authorAllowList;
  private int inputDebounceMs = 0;
  private VPinScreen videoScreen = VPinScreen.Topper;
  private PauseMenuStyle style = PauseMenuStyle.embedded;

  private int testDuration = 5;
  private int testGameId = -1;
  private int unpauseDelay = 1000;

  private boolean showIscoredScores = true;
  private boolean showManiaScores = true;

  private String pauseButton;
  private String startButton;
  private String leftButton;
  private String rightButton;
  private String overlayButton;
  private String screenshotButton;
  private String resetButton;
  private String recordingButton;
  private String inputFilterList;

  public int getUnpauseDelay() {
    return unpauseDelay;
  }

  public void setUnpauseDelay(int unpauseDelay) {
    this.unpauseDelay = unpauseDelay;
  }

  private boolean muteOnPause = false;

  private int pauseMenuScreenId = -1;

  public boolean isMuteOnPause() {
    return muteOnPause;
  }

  public void setMuteOnPause(boolean muteOnPause) {
    this.muteOnPause = muteOnPause;
  }

  public int getTestDuration() {
    return testDuration;
  }

  public void setTestDuration(int testDuration) {
    this.testDuration = testDuration;
  }

  public int getTestGameId() {
    return testGameId;
  }

  public void setTestGameId(int testGameId) {
    this.testGameId = testGameId;
  }

  public boolean isShowIscoredScores() {
    return showIscoredScores;
  }

  public void setShowIscoredScores(boolean showIscoredScores) {
    this.showIscoredScores = showIscoredScores;
  }

  public boolean isShowManiaScores() {
    return showManiaScores;
  }

  public void setShowManiaScores(boolean showManiaScores) {
    this.showManiaScores = showManiaScores;
  }

  public int getPauseMenuScreenId() {
    return pauseMenuScreenId;
  }

  public void setPauseMenuScreenId(int pauseMenuScreenId) {
    this.pauseMenuScreenId = pauseMenuScreenId;
  }

  public String getRecordingButton() {
    return recordingButton;
  }

  public void setRecordingButton(String recordingButton) {
    this.recordingButton = recordingButton;
  }

  public String getInputFilterList() {
    return inputFilterList;
  }

  public void setInputFilterList(String inputFilterList) {
    this.inputFilterList = inputFilterList;
  }

  public String getPauseButton() {
    return pauseButton;
  }

  public void setPauseButton(String pauseButton) {
    this.pauseButton = pauseButton;
  }

  public String getStartButton() {
    return startButton;
  }

  public void setStartButton(String startButton) {
    this.startButton = startButton;
  }

  public String getLeftButton() {
    return leftButton;
  }

  public void setLeftButton(String leftButton) {
    this.leftButton = leftButton;
  }

  public String getRightButton() {
    return rightButton;
  }

  public void setRightButton(String rightButton) {
    this.rightButton = rightButton;
  }

  public String getScreenshotButton() {
    return screenshotButton;
  }

  public void setScreenshotButton(String screenshotButton) {
    this.screenshotButton = screenshotButton;
  }

  public String getOverlayButton() {
    return overlayButton;
  }

  public void setOverlayButton(String overlayButton) {
    this.overlayButton = overlayButton;
  }

  public String getResetButton() {
    return resetButton;
  }

  public void setResetButton(String resetButton) {
    this.resetButton = resetButton;
  }

  public PauseMenuStyle getStyle() {
    if(style == null) {
      style = PauseMenuStyle.embedded;
    }
    return style;
  }

  public int getInputDebounceMs() {
    return inputDebounceMs;
  }

  public void setInputDebounceMs(int inputDebounceMs) {
    this.inputDebounceMs = inputDebounceMs;
  }

  public void setStyle(PauseMenuStyle style) {
    this.style = style;
  }

  public VPinScreen getVideoScreen() {
    return videoScreen;
  }

  public void setVideoScreen(VPinScreen videoScreen) {
    this.videoScreen = videoScreen;
  }

  public String getAuthorAllowList() {
    return authorAllowList;
  }

  public void setAuthorAllowList(String authorAllowList) {
    this.authorAllowList = authorAllowList;
  }

  public boolean isUseOverlayKey() {
    return useOverlayKey;
  }

  public void setUseOverlayKey(boolean useOverlayKey) {
    this.useOverlayKey = useOverlayKey;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.PAUSE_MENU_SETTINGS;
  }
}
