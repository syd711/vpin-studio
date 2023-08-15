package de.mephisto.vpin.server.archiving.adapters.vpbm.config;

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
