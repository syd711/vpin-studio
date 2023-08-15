package de.mephisto.vpin.server.archiving.adapters.vpbm.config;

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
