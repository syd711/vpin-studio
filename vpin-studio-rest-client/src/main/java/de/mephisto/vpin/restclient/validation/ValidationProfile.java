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

  public ValidationConfig getOrCreateConfig(int validationCode) {
    Optional<ValidationConfig> config = configurations.stream().filter(v -> v.getValidationCode() == validationCode).findFirst();
    if (config.isPresent()) {
      return config.get();
    }

    ValidationConfig newConfig = new ValidationConfig();
    newConfig.setValidationCode(validationCode);
    newConfig.setOption(ValidatorOption.optional);
    newConfig.setMedia(ValidatorMedia.imageOrVideo);

    switch (validationCode) {
      case GameValidationCode.CODE_NO_AUDIO: {
        newConfig.setMedia(ValidatorMedia.audio);
        break;
      }
      case GameValidationCode.CODE_NO_AUDIO_LAUNCH: {
        newConfig.setMedia(ValidatorMedia.audio);
        break;
      }
      case GameValidationCode.CODE_NO_INFO: {
        newConfig.setMedia(ValidatorMedia.image);
        break;
      }
      case GameValidationCode.CODE_NO_OTHER2: {
        newConfig.setMedia(ValidatorMedia.image);
        break;
      }
      case GameValidationCode.CODE_NO_HELP: {
        newConfig.setMedia(ValidatorMedia.image);
        break;
      }
      case GameValidationCode.CODE_NO_PLAYFIELD: {
        newConfig.setMedia(ValidatorMedia.video);
        newConfig.setOption(ValidatorOption.mandatory);
        break;
      }
      case GameValidationCode.CODE_NO_LOGO: {
        newConfig.setMedia(ValidatorMedia.image);
        break;
      }
      case GameValidationCode.CODE_NO_WHEEL_IMAGE: {
        newConfig.setMedia(ValidatorMedia.image);
        newConfig.setOption(ValidatorOption.mandatory);
        break;
      }
      case GameValidationCode.CODE_NO_LOADING: {
        newConfig.setMedia(ValidatorMedia.video);
        newConfig.setOption(ValidatorOption.mandatory);
        break;
      }
      case GameValidationCode.CODE_NO_BACKGLASS: {
        newConfig.setOption(ValidatorOption.mandatory);
        break;
      }
      default: {
        newConfig.setOption(ValidatorOption.optional);
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
