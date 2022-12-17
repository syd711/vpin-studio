package de.mephisto.vpin.restclient.representations;

public class GameAssetRepresentation {

  private String name;
  private boolean exists;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isExists() {
    return exists;
  }

  public void setExists(boolean exists) {
    this.exists = exists;
  }
}
