package de.mephisto.vpin.restclient;

import java.util.Properties;

public class ObservedProperties {

  private final String bundle;
  private final Properties properties;

  private ObservedPropertyChangeListener changeListener;

  public ObservedProperties(String bundle, Properties properties) {
    this.bundle = bundle;
    this.properties = properties;
  }

  public void setObservedPropertyChangeListener(ObservedPropertyChangeListener observedPropertyChangeListener) {
    changeListener = observedPropertyChangeListener;
  }

  public void set(String key, String value) {
    this.properties.put(key, value);
    if(this.changeListener != null) {
      this.changeListener.changed(bundle, key, value);
    }
  }

  public String getProperty(String s) {
    return this.properties.getProperty(s);
  }
}
