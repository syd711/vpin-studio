package de.mephisto.vpin.server.preferences;

@FunctionalInterface
public interface PreferenceChangedListener {
  void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception;
}
