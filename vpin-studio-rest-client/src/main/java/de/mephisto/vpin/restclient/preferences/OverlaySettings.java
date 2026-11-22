package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

public class OverlaySettings extends JsonSettings {
  public static final String DESIGN_HS_PLAYERS_OFFLINE = "-hs-plrs-offline";

  private String designType = DESIGN_HS_PLAYERS_OFFLINE;

  private String pageUrl;

  private boolean showOnStartup = false;

  private int overlayScreenId = -1;

  public String getDesignType() {
    // the former default design "" (player ranking, offline + online competitions) has been removed,
    // so empty and legacy values are migrated to the closest remaining design
    if (designType == null || designType.isBlank() || designType.equalsIgnoreCase("null")) {
      return DESIGN_HS_PLAYERS_OFFLINE;
    }
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
