package de.mephisto.vpin.restclient.client;

public interface PreferenceChangeListener {
  void preferencesChanged(String key, Object value);
}
