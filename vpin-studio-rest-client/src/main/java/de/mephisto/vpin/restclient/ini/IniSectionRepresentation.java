package de.mephisto.vpin.restclient.ini;

import java.util.LinkedHashMap;
import java.util.Map;

public class IniSectionRepresentation {
  private String name;

  private Map<String, Object> values = new LinkedHashMap<>();

  public String getName() {
    return name;
  }

  public Map<String, Object> getValues() {
    return values;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setValues(Map<String, Object> values) {
    this.values = values;
  }
}
