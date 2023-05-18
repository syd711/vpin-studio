package de.mephisto.vpin.server.backup.adapters.vpinzip.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Emulator {

  public Emulator() {

  }

  public Emulator(String name) {
    this.name = name;
  }

  @JsonProperty("Name")
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
