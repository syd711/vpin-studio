package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;

public class PauseMenuSettings extends JsonSettings {
  private String key;
  private boolean useOverlayKey;
  private boolean useInternalBrowser = false;
  private boolean autoplay = true;
  private String authorAllowList;
  private int videoScreen;
  private boolean renderTutorialLinks = true;

  public boolean isRenderTutorialLinks() {
    return renderTutorialLinks;
  }

  public void setRenderTutorialLinks(boolean renderTutorialLinks) {
    this.renderTutorialLinks = renderTutorialLinks;
  }

  public int getVideoScreen() {
    return videoScreen;
  }

  public void setVideoScreen(int videoScreen) {
    this.videoScreen = videoScreen;
  }

  public boolean isAutoplay() {
    return autoplay;
  }

  public void setAutoplay(boolean autoplay) {
    this.autoplay = autoplay;
  }

  public String getAuthorAllowList() {
    return authorAllowList;
  }

  public void setAuthorAllowList(String authorAllowList) {
    this.authorAllowList = authorAllowList;
  }

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
