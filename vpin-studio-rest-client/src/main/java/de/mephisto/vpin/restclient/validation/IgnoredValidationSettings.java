package de.mephisto.vpin.restclient.validation;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

import java.util.HashMap;
import java.util.Map;

public class IgnoredValidationSettings extends JsonSettings {
  private Map<String, Boolean> ignoredValidators = new HashMap<>();

  public IgnoredValidationSettings() {
    ignoredValidators.put(String.valueOf(GameValidationCode.CODE_PUP_PACK_FILE_MISSING), true);
    ignoredValidators.put(String.valueOf(GameValidationCode.CODE_ALT_SOUND_FILE_MISSING), true);
    ignoredValidators.put(String.valueOf(GameValidationCode.CODE_FORCE_STEREO), true);
  }

  public Map<String, Boolean> getIgnoredValidators() {
    return ignoredValidators;
  }

  public void setIgnoredValidators(Map<String, Boolean> ignoredValidators) {
    this.ignoredValidators = ignoredValidators;
  }

  public boolean isIgnored(String validationCode) {
    return ignoredValidators.containsKey(validationCode) && ignoredValidators.get(validationCode);
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.IGNORED_VALIDATION_SETTINGS;
  }
}
