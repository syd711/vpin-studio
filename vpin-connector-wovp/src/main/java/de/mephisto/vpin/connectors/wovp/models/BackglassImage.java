package de.mephisto.vpin.connectors.wovp.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public class BackglassImage {
  private Map<String, BackglassImageVariant> sizes = new HashMap<>();

  public Map<String, BackglassImageVariant> getSizes() {
    return sizes;
  }

  public void setSizes(Map<String, BackglassImageVariant> sizes) {
    this.sizes = sizes;
  }

  @JsonIgnore
  public BackglassImageVariant getMediumVariant() {
    return sizes.get("medium");
  }
}
