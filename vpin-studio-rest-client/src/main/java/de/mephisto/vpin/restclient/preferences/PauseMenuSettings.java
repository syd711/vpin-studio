package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;

public class PauseMenuSettings extends JsonSettings {
  private String key;
  private boolean useOverlayKey;
  private boolean useInternalBrowser = false;

  public boolean isUseInternalBrowser() {
    return useInternalBrowser;
  }

  public void setUseInternalBrowser(boolean useInternalBrowser) {
    this.useInternalBrowser = useInternalBrowser;
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
