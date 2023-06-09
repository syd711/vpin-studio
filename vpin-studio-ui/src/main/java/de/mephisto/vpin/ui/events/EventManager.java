package de.mephisto.vpin.ui.events;

import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.descriptors.JobDescriptor;
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
}
