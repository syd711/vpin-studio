package de.mephisto.vpin.restclient.wovp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class WOVPSettings extends JsonSettings {
  private String apiKey1;
  private String apiKey2;
  private String apiKey3;
  private String apiKey4;
  private String apiKey5;
  private boolean badgeEnabled;
  private boolean enabled;
  private boolean taggingEnabled;
  private boolean resetHighscores = true;
  private boolean useScoreSubmitter = true;
  private List<String> tags = new ArrayList<>();

  @JsonIgnore
  public String getApiKey(int activeUser) {
    switch (activeUser) {
      case 1: {
        return getApiKey1();
      }
      case 2: {
        return getApiKey2();
      }
      case 3: {
        return getApiKey3();
      }
      case 4: {
        return getApiKey4();
      }
      case 5: {
        return getApiKey5();
      }
      default: {
        throw new UnsupportedOperationException("No valid user id set.");
      }
    }
  }

  @JsonIgnore
  public String getAnyApiKey() {
    int index = 1;
    while (index <= 5) {
      String apiKey = getApiKey(index);
      if (!StringUtils.isEmpty(apiKey)) {
        return apiKey;
      }
      index++;
    }
    return null;
  }

  @JsonIgnore
  public List<String> getApiKeys() {
    List<String> result = new ArrayList<>();
    int index = 1;
    while (index <= 5) {
      String apiKey = getApiKey(index);
      if (!StringUtils.isEmpty(apiKey)) {
        result.add(apiKey);
      }
      index++;
    }
    return result;
  }

  public boolean isUseScoreSubmitter() {
    return useScoreSubmitter;
  }

  public void setUseScoreSubmitter(boolean useScoreSubmitter) {
    this.useScoreSubmitter = useScoreSubmitter;
  }

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

  public String getApiKey1() {
    return apiKey1;
  }

  public void setApiKey1(String apiKey1) {
    this.apiKey1 = apiKey1;
  }

  public String getApiKey2() {
    return apiKey2;
  }

  public void setApiKey2(String apiKey2) {
    this.apiKey2 = apiKey2;
  }

  public String getApiKey3() {
    return apiKey3;
  }

  public void setApiKey3(String apiKey3) {
    this.apiKey3 = apiKey3;
  }

  public String getApiKey4() {
    return apiKey4;
  }

  public void setApiKey4(String apiKey4) {
    this.apiKey4 = apiKey4;
  }

  public String getApiKey5() {
    return apiKey5;
  }

  public void setApiKey5(String apiKey5) {
    this.apiKey5 = apiKey5;
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
    return !StringUtils.isEmpty(apiKey1) ||
        !StringUtils.isEmpty(apiKey2) ||
        !StringUtils.isEmpty(apiKey3) ||
        !StringUtils.isEmpty(apiKey4) ||
        !StringUtils.isEmpty(apiKey5);
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.WOVP_SETTINGS;
  }
}
