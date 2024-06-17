package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.popper.PopperScreen;

public class PauseMenuSettings extends JsonSettings {
  private String key;
  private boolean useOverlayKey;
  private String authorAllowList;
  private int inputDebounceMs = 0;
  private PopperScreen videoScreen = PopperScreen.Topper;
  private PauseMenuStyle style = PauseMenuStyle.embedded;

  private int customLaunchKey;
  private int customStartKey;
  private int customLeftKey;
  private int customRightKey;
  private int customOverlayKey;


  private String customLaunchButton;
  private String customStartButton;
  private String customLeftButton;
  private String customRightButton;
  private String customOverlayButton;

  public String getCustomOverlayButton() {
    return customOverlayButton;
  }

  public void setCustomOverlayButton(String customOverlayButton) {
    this.customOverlayButton = customOverlayButton;
  }

  public int getCustomOverlayKey() {
    return customOverlayKey;
  }

  public void setCustomOverlayKey(int customOverlayKey) {
    this.customOverlayKey = customOverlayKey;
  }

  public String getCustomLaunchButton() {
    return customLaunchButton;
  }

  public void setCustomLaunchButton(String customLaunchButton) {
    this.customLaunchButton = customLaunchButton;
  }

  public String getCustomStartButton() {
    return customStartButton;
  }

  public void setCustomStartButton(String customStartButton) {
    this.customStartButton = customStartButton;
  }

  public String getCustomLeftButton() {
    return customLeftButton;
  }

  public void setCustomLeftButton(String customLeftButton) {
    this.customLeftButton = customLeftButton;
  }

  public String getCustomRightButton() {
    return customRightButton;
  }

  public void setCustomRightButton(String customRightButton) {
    this.customRightButton = customRightButton;
  }

  public int getCustomLaunchKey() {
    return customLaunchKey;
  }

  public void setCustomLaunchKey(int customLaunchKey) {
    this.customLaunchKey = customLaunchKey;
  }

  public int getCustomStartKey() {
    return customStartKey;
  }

  public void setCustomStartKey(int customStartKey) {
    this.customStartKey = customStartKey;
  }

  public int getCustomLeftKey() {
    return customLeftKey;
  }

  public void setCustomLeftKey(int customLeftKepy) {
    this.customLeftKey = customLeftKey;
  }

  public int getCustomRightKey() {
    return customRightKey;
  }

  public void setCustomRightKey(int customRightKey) {
    this.customRightKey = customRightKey;
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

  public PopperScreen getVideoScreen() {
    return videoScreen;
  }

  public void setVideoScreen(PopperScreen videoScreen) {
    this.videoScreen = videoScreen;
  }

  public String getAuthorAllowList() {
    return authorAllowList;
  }

  public void setAuthorAllowList(String authorAllowList) {
    this.authorAllowList = authorAllowList;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public boolean isUseOverlayKey() {
    return useOverlayKey;
  }

  public void setUseOverlayKey(boolean useOverlayKey) {
    this.useOverlayKey = useOverlayKey;
  }
}
