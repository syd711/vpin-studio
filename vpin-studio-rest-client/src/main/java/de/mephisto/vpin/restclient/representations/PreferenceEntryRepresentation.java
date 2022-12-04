package de.mephisto.vpin.restclient.representations;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
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

  public boolean getBooleanValue(boolean defaultValue) {
    if (StringUtils.isEmpty(value)) {
      return defaultValue;
    }
    return Boolean.parseBoolean(value);
  }

  public boolean getBooleanValue() {
    if (StringUtils.isEmpty(value)) {
      return false;
    }
    return Boolean.parseBoolean(value);
  }

  public List<String> getCSVValue() {
    if (value == null) {
      return new ArrayList<>();
    }
    String[] split = value.split(",");
    return new ArrayList<>(Arrays.asList(split));
  }

  public void setValue(String value) {
    this.value = value;
  }
}
