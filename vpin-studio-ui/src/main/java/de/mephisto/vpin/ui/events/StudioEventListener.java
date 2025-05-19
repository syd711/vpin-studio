package de.mephisto.vpin.ui.events;

import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.List;

public interface StudioEventListener {
  default void jobFinished(@NonNull JobFinishedEvent event) {

  }

  default void tableChanged(int id, @Nullable String rom, @Nullable String gameName) {

  }

  default void backglassChanged(int emulatorId, String b2sFileName) {

  }

  default void vpsTableChanged(@NonNull String vpsTableId) {

  }

  default void tablesChanged() {

  }

  default void tablesSelected(List<GameRepresentation> games) {

  }

  default void tableUploaded(UploadDescriptor uploadDescriptor) {

  }

  default void repositoryUpdated() {

  }

  default void preferencesChanged(PreferenceType preferenceType) {

  }

  default void thirdPartyVersionUpdated(@NonNull ComponentType type) {

  }

  default void alxDataUpdated(@Nullable GameRepresentation game) {

  }

  default void maintenanceEnabled(boolean b) {

  }
}
