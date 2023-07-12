package de.mephisto.vpin.ui.events;


import edu.umd.cs.findbugs.annotations.NonNull;

public interface StudioEventListener {
  default void jobFinished(@NonNull JobFinishedEvent event) {

  }

  default void tableChanged(int id) {

  }

  default void repositoryUpdated() {

  }

  default void preferencesChanged() {

  }
}
