package de.mephisto.vpin.restclient.representations;

public class EmulatorRepresentation {

  private String name;
  private boolean visualPinball;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isVisualPinball() {
    return visualPinball;
  }

  public void setVisualPinball(boolean visualPinball) {
    this.visualPinball = visualPinball;
  }
}
