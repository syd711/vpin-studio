package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

public class PauseMenuSettings extends JsonSettings {
  private boolean useOverlayKey;
  private String authorAllowList;
  private int inputDebounceMs = 0;
  private VPinScreen videoScreen = VPinScreen.Topper;
  private PauseMenuStyle style = PauseMenuStyle.embedded;

  private String pauseButton;
  private String startButton;
  private String leftButton;
  private String rightButton;
  private String overlayButton;
  private String resetButton;

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
}
