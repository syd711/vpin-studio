package de.mephisto.vpin.ui.events;


import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public interface StudioEventListener {
  default void jobFinished(@NonNull JobFinishedEvent event) {

  }

  default void tableChanged(int id, @Nullable String rom) {

  }

  default void tablesChanged() {

  }

  default void repositoryUpdated() {

  }

  default void preferencesChanged() {

  }

  default void maintenanceEnabled(boolean b) {

  }
}
