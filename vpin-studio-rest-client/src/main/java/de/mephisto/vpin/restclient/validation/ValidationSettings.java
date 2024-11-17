package de.mephisto.vpin.restclient.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

import java.util.ArrayList;
import java.util.List;

public class ValidationSettings extends JsonSettings {
  private List<ValidationProfile> validationProfiles = new ArrayList<>();

  public List<ValidationProfile> getValidationProfiles() {
    return validationProfiles;
  }

  public void setValidationProfiles(List<ValidationProfile> validationProfiles) {
    this.validationProfiles = validationProfiles;
  }

  @JsonIgnore
  public ValidationProfile getDefaultProfile() {
    if(validationProfiles.isEmpty()) {
      ValidationProfile defaultProfile = new ValidationProfile();
      defaultProfile.setName(ValidationProfile.DEFAULT);
      validationProfiles.add(defaultProfile);
    }

    return validationProfiles.stream().filter(p -> p.getName().equals(ValidationProfile.DEFAULT)).findFirst().get();
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.VALIDATION_SETTINGS;
  }
}
