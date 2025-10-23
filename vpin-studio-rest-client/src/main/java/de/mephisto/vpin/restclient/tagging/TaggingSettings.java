package de.mephisto.vpin.restclient.tagging;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaggingSettings extends JsonSettings {

  private boolean autoTagScreensEnabled;
  private boolean autoTagTablesEnabled;
  private boolean autoTagBackglassEnabled;
  private List<String> screenTags = new ArrayList<>();
  private List<String> tableTags = new ArrayList<>();
  private List<String> backglassTags = new ArrayList<>();

  private Map<String, Boolean> autoTaggedScreens = new HashMap<>();

//  static {
//    autoTaggedScreens.put(VPinScreen.PlayField.name(), true)
//  }

  public Map<String, Boolean> getAutoTaggedScreens() {
    return autoTaggedScreens;
  }

  public boolean isAutoTagBackglassEnabled() {
    return autoTagBackglassEnabled;
  }

  public void setAutoTagBackglassEnabled(boolean autoTagBackglassEnabled) {
    this.autoTagBackglassEnabled = autoTagBackglassEnabled;
  }

  public List<String> getBackglassTags() {
    return backglassTags;
  }

  public void setBackglassTags(List<String> backglassTags) {
    this.backglassTags = backglassTags;
  }

  public boolean isAutoTagTablesEnabled() {
    return autoTagTablesEnabled;
  }

  public void setAutoTagTablesEnabled(boolean autoTagTablesEnabled) {
    this.autoTagTablesEnabled = autoTagTablesEnabled;
  }

  public List<String> getTableTags() {
    return tableTags;
  }

  public void setTableTags(List<String> tableTags) {
    this.tableTags = tableTags;
  }

  public void setAutoTaggedScreens(Map<String, Boolean> autoTaggedScreens) {
    this.autoTaggedScreens = autoTaggedScreens;
  }

  public List<String> getScreenTags() {
    return screenTags;
  }

  public void setScreenTags(List<String> screenTags) {
    this.screenTags = screenTags;
  }

  public boolean isAutoTagScreensEnabled() {
    return autoTagScreensEnabled;
  }

  public void setAutoTagScreensEnabled(boolean autoTagScreensEnabled) {
    this.autoTagScreensEnabled = autoTagScreensEnabled;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.TAGGING_SETTINGS;
  }
}
