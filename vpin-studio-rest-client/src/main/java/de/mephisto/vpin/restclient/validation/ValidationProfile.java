package de.mephisto.vpin.restclient.validation;

import de.mephisto.vpin.restclient.popper.PopperScreen;

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
    newConfig.setOption(ValidatorOption.optional);

    switch (validationCode) {
      case GameValidationCode.CODE_NO_AUDIO:
      case GameValidationCode.CODE_NO_AUDIO_LAUNCH: {
        newConfig.setMedia(ValidatorMedia.audio);
        break;
      }
      case GameValidationCode.CODE_NO_WHEEL_IMAGE:
      case GameValidationCode.CODE_NO_INFO:
      case GameValidationCode.CODE_NO_OTHER2: {
        newConfig.setMedia(ValidatorMedia.image);
        break;
      }
      default: {
        newConfig.setMedia(ValidatorMedia.imageOrVideo);
      }
    }
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
