package de.mephisto.vpin.restclient.wovp;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

import java.util.ArrayList;
import java.util.List;

public class WOVPSettings extends JsonSettings {
  private String apiKey;
  private boolean badgeEnabled;
  private boolean enabled;
  private boolean taggingEnabled;
  private List<String> tags = new ArrayList<>();

  public boolean isTaggingEnabled() {
    return taggingEnabled;
  }

  public void setTaggingEnabled(boolean taggingEnabled) {
    this.taggingEnabled = taggingEnabled;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public boolean isBadgeEnabled() {
    return badgeEnabled;
  }

  public void setBadgeEnabled(boolean badgeEnabled) {
    this.badgeEnabled = badgeEnabled;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.WOVP_SETTINGS;
  }
}
