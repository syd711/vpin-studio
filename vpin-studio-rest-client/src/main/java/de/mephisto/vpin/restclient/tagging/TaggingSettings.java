package de.mephisto.vpin.restclient.tagging;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

import java.util.ArrayList;
import java.util.List;

public class TaggingSettings extends JsonSettings {

  private boolean autoTagScreensEnabled;
  private boolean autoTagTablesEnabled;
  private boolean autoTagBackglassEnabled;
  private List<String> screenTags = new ArrayList<>();
  private List<String> tableTags = new ArrayList<>();
  private List<String> backglassTags = new ArrayList<>();

  private List<VPinScreen> taggedScreens = new ArrayList<>();

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

  public List<VPinScreen> getTaggedScreens() {
    return taggedScreens;
  }

  public void setTaggedScreens(List<VPinScreen> taggedScreens) {
    this.taggedScreens = taggedScreens;
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
