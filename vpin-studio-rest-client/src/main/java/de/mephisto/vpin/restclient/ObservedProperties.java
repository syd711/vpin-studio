package de.mephisto.vpin.restclient;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ObservedProperties {

  private final String bundle;
  private final Properties properties;

  private List<ObservedPropertyChangeListener> changeListeners = new ArrayList<>();

  public ObservedProperties(String bundle, Properties properties) {
    this.bundle = bundle;
    this.properties = properties;
  }

  public void addObservedPropertyChangeListener(ObservedPropertyChangeListener observedPropertyChangeListener) {
    changeListeners.add(observedPropertyChangeListener);
  }

  public void set(String key, String value) {
    this.properties.put(key, value);
    for (ObservedPropertyChangeListener changeListener : this.changeListeners) {
      changeListener.changed(bundle, key, value);
    }
  }

  public int getProperty(String s, int defaultValue) {
    String value = this.properties.getProperty(s);
    if(!StringUtils.isEmpty(value)) {
      return Integer.parseInt(value);
    }
    return defaultValue;
  }

  public boolean getProperty(String s, boolean defaultValue) {
    String value = this.properties.getProperty(s);
    if(!StringUtils.isEmpty(value)) {
      return Boolean.parseBoolean(value);
    }
    return defaultValue;
  }

  public String getProperty(String s, String defaultValue) {
    String value = this.properties.getProperty(s);
    if(!StringUtils.isEmpty(value)) {
      return value;
    }
    return defaultValue;
  }
}
