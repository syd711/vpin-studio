package de.mephisto.vpin.ui.events;

import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class EventManager {

  private static EventManager instance = new EventManager();

  private List<StudioEventListener> listeners = new ArrayList<>();

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
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.tableChanged(tableId, rom);
      }
    }).start();
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

  public void notifyPreferenceChanged() {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.preferencesChanged();
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

  public void notify3rdPartyVersionUpdate(ComponentType type) {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.thirdPartyVersionUpdated(type);
      }
    }).start();
  }

  public void removeListener(StudioEventListener listener) {
    this.listeners.remove(listener);
  }
}
