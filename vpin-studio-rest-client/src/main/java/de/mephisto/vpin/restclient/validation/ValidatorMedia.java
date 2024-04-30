package de.mephisto.vpin.restclient.validation;

public enum ValidatorMedia {
  audio("Audio"),
  video("Video"),
  image("Image"),
  imageOrVideo("Image or Video");


  private final String displayName;

  ValidatorMedia(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  @Override
  public String toString() {
    return getDisplayName();
  }
}
