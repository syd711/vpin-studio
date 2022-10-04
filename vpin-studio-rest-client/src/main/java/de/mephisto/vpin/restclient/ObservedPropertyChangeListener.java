package de.mephisto.vpin.restclient;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface ObservedPropertyChangeListener {

  void changed(@NonNull String propertiesName, @NonNull String key, @Nullable String updatedValue);
}
