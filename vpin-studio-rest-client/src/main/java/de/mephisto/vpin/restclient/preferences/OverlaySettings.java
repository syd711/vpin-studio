package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

public class OverlaySettings extends JsonSettings {
  private String designType;

  private String pageUrl;

  private boolean showOnStartup = false;

  private int overlayScreenId = -1;

  public String getDesignType() {
    return designType;
  }

  public void setDesignType(String designType) {
    this.designType = designType;
  }

  public String getPageUrl() {
    return pageUrl;
  }

  public void setPageUrl(String pageUrl) {
    this.pageUrl = pageUrl;
  }

  public boolean isShowOnStartup() {
    return showOnStartup;
  }

  public void setShowOnStartup(boolean showOnStartup) {
    this.showOnStartup = showOnStartup;
  }

  public int getOverlayScreenId() {
    return overlayScreenId;
  }

  public void setOverlayScreenId(int overlayScreenId) {
    this.overlayScreenId = overlayScreenId;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.OVERLAY_SETTINGS;
  }
}
