package de.mephisto.vpin.restclient.representations;

public class VpaSourceRepresentation {
  private String type;
  private String location;

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
}
