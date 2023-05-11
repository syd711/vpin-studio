package de.mephisto.vpin.ui.events;


import edu.umd.cs.findbugs.annotations.NonNull;

public interface StudioEventListener {
  default void onTableBackedUp(@NonNull TableBackedUpEvent event) {

  }

  default void onArchiveInstalled(@NonNull ArchiveInstalledEvent event) {

  }

  default void onArchiveCopiedToRepository() {

  }

  default void onArchiveDownload() {

  }

  default void onArchiveSourceUpdate() {

  }
}
