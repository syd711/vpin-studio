package de.mephisto.vpin.restclient.representations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PreferenceEntryRepresentation {
  private String key;
  private String value;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public String getNotNullValue() {
    if (value == null) {
      return "";
    }
    return value;
  }

  public List<String> getCSVValue() {
    if(value == null) {
      return Collections.emptyList();
    }
    String[] split = value.split(",");
    return Arrays.asList(split);
  }

  public void setValue(String value) {
    this.value = value;
  }
}
