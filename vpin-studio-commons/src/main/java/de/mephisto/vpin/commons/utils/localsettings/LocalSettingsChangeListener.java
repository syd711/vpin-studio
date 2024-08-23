package de.mephisto.vpin.commons.utils.localsettings;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface LocalSettingsChangeListener {

  void localSettingsChanged(@NonNull String key, @Nullable String value);
}
