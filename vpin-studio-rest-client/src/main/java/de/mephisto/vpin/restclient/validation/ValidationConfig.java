package de.mephisto.vpin.restclient.validation;

public class ValidationConfig {
  private int validationCode;
  private ValidatorOption option;
  private ValidatorMedia media;

  public int getValidationCode() {
    return validationCode;
  }

  public void setValidationCode(int validationCode) {
    this.validationCode = validationCode;
  }

  public ValidatorOption getOption() {
    return option;
  }

  public void setOption(ValidatorOption option) {
    this.option = option;
  }

  public ValidatorMedia getMedia() {
    return media;
  }

  public void setMedia(ValidatorMedia media) {
    this.media = media;
  }
}
