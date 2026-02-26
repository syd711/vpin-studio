package de.mephisto.vpin.server.nvrams.parser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class NVRamMappings extends NVRamObject {
    
  private Map<String, NVRamMapping> mappings = new LinkedHashMap<>();

  @JsonAnySetter
  void setMappings(String key, NVRamMapping value) {
    mappings.put(key, value);
  }

  public List<String> keySet() {
    return new ArrayList<>(mappings.keySet());
  }

  public NVRamMapping get(String key) {
    return mappings.get(key);
  }
}
