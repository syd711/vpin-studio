package de.mephisto.vpin.restclient.client;

public interface PreferenceChangeListener {
  void preferenceChanged(String key, Object value);
}
