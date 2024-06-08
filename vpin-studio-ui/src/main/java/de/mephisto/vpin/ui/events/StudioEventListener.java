package de.mephisto.vpin.ui.events;


import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface StudioEventListener {
  default void jobFinished(@NonNull JobFinishedEvent event) {

  }

  default void tableChanged(int id, @Nullable String rom, @Nullable String gameName) {

  }

  default void tablesChanged() {

  }

  default void repositoryUpdated() {

  }

  default void preferencesChanged(PreferenceType preferenceType) {

  }

  default void thirdPartyVersionUpdated(@NonNull ComponentType type) {

  }

  default void maintenanceEnabled(boolean b) {

  }
}
