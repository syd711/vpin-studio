package de.mephisto.vpin.restclient.util.properties;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ObservedProperties {

  private final String bundle;
  private final Properties properties;

  private List<ObservedPropertyChangeListener> changeListeners = new ArrayList<>();

  private ObservedPropertyChangeListener observer;

  public ObservedProperties(String bundle, Properties properties) {
    this.bundle = bundle;
    this.properties = properties;
  }

  public void setObserver(ObservedPropertyChangeListener observer) {
    this.observer = observer;
  }

  public void addObservedPropertyChangeListener(ObservedPropertyChangeListener observedPropertyChangeListener) {
    changeListeners.add(observedPropertyChangeListener);
  }

  public void set(String key, String value) {
    this.properties.put(key, value);
    this.observer.changed(bundle, key, Optional.of(value));
  }

  public void set(Map<String, String> values) {
    this.properties.putAll(values);
    this.observer.changed(bundle, values);
  }

  public void notifyChange(String key, String value) {
    for (ObservedPropertyChangeListener changeListener : this.changeListeners) {
      changeListener.changed(bundle, key, Optional.of(value));
    }
  }

  public void notifyChange(Map<String, String> values) {
    for (ObservedPropertyChangeListener changeListener : this.changeListeners) {
      changeListener.changed(bundle, values);
    }
  }

  public int getProperty(String s, int defaultValue) {
    Object o = this.properties.get(s);
    if (o != null && !StringUtils.isEmpty(String.valueOf(o))) {
      return Integer.parseInt(String.valueOf(o));
    }
    return defaultValue;
  }

  public boolean getProperty(String s, boolean defaultValue) {
    Object o = this.properties.get(s);
    if (o != null && !StringUtils.isEmpty(String.valueOf(o))) {
      return Boolean.parseBoolean(String.valueOf(o));
    }
    return defaultValue;
  }

  public String getProperty(String s, String defaultValue) {
    String value = this.properties.getProperty(s);
    if (!StringUtils.isEmpty(value)) {
      return value;
    }
    return defaultValue;
  }
}
