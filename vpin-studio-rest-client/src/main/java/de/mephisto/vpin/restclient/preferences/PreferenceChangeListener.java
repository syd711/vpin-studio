package de.mephisto.vpin.restclient.preferences;

public interface PreferenceChangeListener {
  void preferencesChanged(String key, Object value);
}
