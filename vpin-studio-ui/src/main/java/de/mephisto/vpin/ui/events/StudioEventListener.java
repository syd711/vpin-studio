package de.mephisto.vpin.ui.events;


import edu.umd.cs.findbugs.annotations.NonNull;

public interface StudioEventListener {
  default void onVpaExport(@NonNull VpaExportedEvent event) {

  }
}
