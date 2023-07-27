package de.mephisto.vpin.restclient;

import java.util.Map;

public class IniSettings {

  private Map<String, Object> settings;
  private IniSettingsChangeListener changeListener;

  public IniSettings(Map<String, Object> settings) {
    this.settings = settings;
  }

  public IniSettingsChangeListener getChangeListener() {
    return changeListener;
  }

  public void setChangeListener(IniSettingsChangeListener changeListener) {
    this.changeListener = changeListener;
  }

  public Map<String, Object> getSettings() {
    return settings;
  }

  public int getInt(String key, int defaultValue) {
    if (settings.containsKey(key)) {
      String value = String.valueOf(settings.get(key)).trim();
      if (value.length() > 0) {
        return Integer.parseInt(value);
      }
    }
    return defaultValue;
  }

  public boolean getBoolean(String key) {
    if (settings.containsKey(key)) {
      String value = String.valueOf(settings.get(key)).trim();
      if (value.length() > 0) {
        return Integer.parseInt(value) == 1;
      }
    }
    return false;
  }

  public void set(String key, boolean b) {
    settings.put(key, b ? 1 : 0);
    if (changeListener != null) {
      changeListener.changed(key, b);
    }
  }

  public void set(String key, String value) {
    settings.put(key, value);
    if (changeListener != null) {
      changeListener.changed(key, value);
    }
  }

  public void set(String key, int value) {
    settings.put(key, value);
    if (changeListener != null) {
      changeListener.changed(key, value);
    }
  }

  public int getInt(String key) {
    if (settings.containsKey(key)) {
      String value = String.valueOf(settings.get(key)).trim();
      if (value.length() > 0) {
        return Integer.parseInt(value);
      }
    }
    return -1;
  }

  public void setValues(Map<String, Object> values) {
    this.settings.putAll(values);
    changeListener.changed("", values);
  }

  public String getString(String key) {
    if (settings.containsKey(key)) {
      return String.valueOf(settings.get(key)).trim();
    }
    return "";
  }
}
