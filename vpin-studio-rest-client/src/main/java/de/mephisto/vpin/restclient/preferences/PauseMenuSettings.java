package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.popper.PopperScreen;

public class PauseMenuSettings extends JsonSettings {
  private String key;
  private boolean useOverlayKey;
  private String authorAllowList;
  private int inputDebounceMs = 0;
  private PopperScreen videoScreen = PopperScreen.Topper;

  private int customLaunchKey;
  private int customStartKey;
  private int customLeftKey;
  private int customRightKey;

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

  public void setCustomLeftKey(int customLeftKey) {
    this.customLeftKey = customLeftKey;
  }

  public int getCustomRightKey() {
    return customRightKey;
  }

  public void setCustomRightKey(int customRightKey) {
    this.customRightKey = customRightKey;
  }

  private PauseMenuStyle style = PauseMenuStyle.embedded;

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
