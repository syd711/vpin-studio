package de.mephisto.vpin.ui.events;

import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;

import java.util.ArrayList;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class EventManager {

  private static EventManager instance;

  private List<StudioEventListener> listeners = new ArrayList<>();

  /**
   * Recreate a new EventManager each time studio starts
   */
  public static void initialize() {
    instance = new EventManager();
  }

  public static EventManager getInstance() {
    return instance;
  }

  public void addListener(@NonNull StudioEventListener listener) {
    this.listeners.add(listener);
  }

  private boolean maintenanceMode = false;

  public boolean isMaintenanceMode() {
    return maintenanceMode;
  }

  public void notifyMaintenanceMode(boolean enabled) {
    maintenanceMode = enabled;
    client.getSystemService().setMaintenanceMode(maintenanceMode);
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.maintenanceEnabled(enabled);
      }
    }).start();
  }

  /**
   * If the ROM name is set, all tables with this ROM name are invalidated.
   *
   * @param tableId the id of the table
   * @param rom     the ROM name of the table
   */
  public void notifyTableChange(int tableId, @Nullable String rom) {
    notifyTableChange(tableId, rom, null);
  }

  public void notifyTableChange(int tableId, @Nullable String rom, @Nullable String gameName) {
    Platform.runLater(() -> {
      for (StudioEventListener listener : listeners) {
        listener.tableChanged(tableId, rom, gameName);
      }
    });
  }

  public void notifyBackglassChange(int emulatorId, String b2sFileName) {
    Platform.runLater(() -> {
      for (StudioEventListener listener : listeners) {
        listener.backglassChanged(emulatorId, b2sFileName);
      }
    });
  }

  public void notifyJobFinished(JobDescriptor descriptor) {
    JobType type = descriptor.getJobType();
    notifyJobFinished(type, descriptor.getGameId());
  }

  public void notifyJobFinished(JobType type) {
    JobFinishedEvent event = new JobFinishedEvent(type);
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.jobFinished(event);
      }
    }).start();
  }

  public void notifyJobFinished(JobType type, int gameId) {
    JobFinishedEvent event = new JobFinishedEvent(type, gameId);
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.jobFinished(event);
      }
    }).start();
  }

  public void notifyRepositoryUpdate() {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.repositoryUpdated();
      }
    }).start();
  }

  public void notifyTableSelectionChanged(List<GameRepresentation> games) {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.tablesSelected(games);
      }
    }).start();
  }

  public void notifyAlxUpdate(@Nullable GameRepresentation game) {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.alxDataUpdated(game);
      }
    }).start();
  }

  public void notifyPreferenceChanged(PreferenceType preferenceType) {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.preferencesChanged(preferenceType);
      }
    }).start();
  }

  public void notifyTablesChanged() {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.tablesChanged();
      }
    }).start();
  }

  public void notifyTableUploaded(UploadDescriptor result) {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.tableUploaded(result);
      }
    }).start();
  }

  public void notify3rdPartyVersionUpdate(ComponentType type) {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.thirdPartyVersionUpdated(type);
      }
    }).start();
  }

  public void notifyVpsTableChange(String id) {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.vpsTableChanged(id);
      }
    }).start();
  }

  public void removeListener(StudioEventListener listener) {
    this.listeners.remove(listener);
  }
}
