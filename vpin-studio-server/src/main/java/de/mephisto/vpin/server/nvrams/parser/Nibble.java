package de.mephisto.vpin.server.nvrams.parser;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum Nibble {
  @JsonProperty("both")
  BOTH, 
  @JsonProperty("low")
  LOW, 
  @JsonProperty("high")
  HIGH;


  @JsonCreator
  public static Nibble forValue(String value) {
    return Nibble.fromString(value);
  }

  public static Nibble fromString(String value) {
    if (value == null || value.equals("both")) return BOTH;
    if (value.equals("low")) return LOW;
    if (value.equals("high")) return HIGH;
    throw new IllegalArgumentException("Invalid nibble value: " + value);
  }
}
