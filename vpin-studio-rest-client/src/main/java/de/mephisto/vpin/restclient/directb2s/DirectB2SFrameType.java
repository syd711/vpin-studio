package de.mephisto.vpin.restclient.directb2s;

public enum DirectB2SFrameType {

  // keep USE_FRAME first
  USE_FRAME("Use Picture", "Use the picture frame."),
  AMBILIGHT("Ambilight", "Uses the pixels on the edge of the backglass to generate the frame."),
  BLURRED("Blurred", "Creates a blurred zoom of the backglass."),
  MIRROR("Mirror", "Creates a blurred mirror reflection."),
  GRADIENT("Gradient", "Calculate a dominant color of the image and use it to draw a gradient to black");

  private String name;
  private String description;

  DirectB2SFrameType(String name, String description) {
    this.name = name;
    this.description = description;
  }

  public String toString() {
    return name;  // used in combo as label
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }
}
