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

  public int getIntValue() {
    try {
      if(value != null) {
        return Integer.parseInt(value);
      }
    }
    catch (Exception e) {
      //ignore
    }
    return 0;
  }

  public long getLongValue() {
    try {
      if(value != null) {
        return Long.parseLong(value);
      }
    }
    catch (Exception e) {
      //ignore
    }
    return 0;
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

  public String getOptionValue(String optionName) {
    if(!StringUtils.isEmpty(value)) {
      String[] split = value.split(";");
      for (String optionEntry : split) {
        String[] entry = optionEntry.split("=");
        if(entry.length == 2) {
          String key = entry[0];
          if(key.equals(optionName)) {
            return entry[1];
          }
        }
      }

    }
    return null;
  }
}
