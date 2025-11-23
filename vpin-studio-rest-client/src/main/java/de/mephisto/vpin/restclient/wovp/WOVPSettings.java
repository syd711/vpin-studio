package de.mephisto.vpin.restclient.wovp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class WOVPSettings extends JsonSettings {
  private String apiKey;
  private boolean badgeEnabled;
  private boolean enabled;
  private boolean taggingEnabled;
  private boolean resetHighscores = true;
  private List<String> tags = new ArrayList<>();

  public boolean isResetHighscores() {
    return resetHighscores;
  }

  public void setResetHighscores(boolean resetHighscores) {
    this.resetHighscores = resetHighscores;
  }

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

  @JsonIgnore
  public boolean isApiKeySet() {
    return !StringUtils.isEmpty(apiKey);
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.WOVP_SETTINGS;
  }
}
