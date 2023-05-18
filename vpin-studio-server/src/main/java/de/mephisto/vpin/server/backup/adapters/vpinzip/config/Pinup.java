package de.mephisto.vpin.server.backup.adapters.vpinzip.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Pinup {
  @JsonProperty("PinupDir")
  private String pinupDir = "PinUPSystem";

  public String getPinupDir() {
    return pinupDir;
  }

  public void setPinupDir(String pinupDir) {
    this.pinupDir = pinupDir;
  }
}
