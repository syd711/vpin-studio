package de.mephisto.vpin.ui.events;

import de.mephisto.vpin.restclient.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class EventManager {

  private static EventManager instance = new EventManager();

  private List<StudioEventListener> listeners = new ArrayList<>();

  public static EventManager getInstance() {
    return instance;
  }

  public void addListener(@NonNull StudioEventListener listener) {
    this.listeners.add(listener);
  }

  public void notifyTableChange(int tableId) {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.tableChanged(tableId);
      }
    }).start();
  }

  public void notifyJobFinished(JobDescriptor descriptor) {
    JobType type = descriptor.getJobType();
    notifyJobFinished(type);
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

  public void removeListener(StudioEventListener listener) {
    this.listeners.remove(listener);
  }
}
