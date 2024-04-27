package de.mephisto.vpin.restclient.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ValidationProfile {
  public final static String DEFAULT = "default";
  private String name;
  private List<ValidationConfig> configurations = new ArrayList<>();

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Optional<ValidationConfig> getConfig(int validatorId) {
    return configurations.stream().filter(v -> v.getValidationCode() == validatorId).findFirst();
  }

  public ValidationConfig getOrCreateConfig(int validationCode) {
    Optional<ValidationConfig> config = getConfig(validationCode);
    if (config.isPresent()) {
      return config.get();
    }
    ValidationConfig newConfig = new ValidationConfig();
    newConfig.setValidationCode(validationCode);
    configurations.add(newConfig);
    return newConfig;
  }

  public List<ValidationConfig> getConfigurations() {
    return configurations;
  }

  public void setConfigurations(List<ValidationConfig> configurations) {
    this.configurations = configurations;
  }
}
