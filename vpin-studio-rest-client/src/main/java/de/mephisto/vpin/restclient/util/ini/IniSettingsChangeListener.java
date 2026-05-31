package de.mephisto.vpin.restclient.util.ini;

import org.jspecify.annotations.NonNull;

public interface IniSettingsChangeListener {
  void changed(@NonNull String key, @NonNull Object value);
}
