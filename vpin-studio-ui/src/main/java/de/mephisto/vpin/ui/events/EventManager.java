package de.mephisto.vpin.ui.events;

import de.mephisto.vpin.restclient.JobDescriptor;
import de.mephisto.vpin.restclient.JobType;
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

  public void notifyVpaExport(String uuid) {
    new Thread(() -> {
      VpaExportedEvent event = new VpaExportedEvent(uuid);
      for (StudioEventListener listener : listeners) {
        listener.onVpaExport(event);
      }
    }).start();
  }

  public void notifyVpaSourceUpdate() {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.onVpaSourceUpdate();
      }
    }).start();
  }

  public void notifyVpaDownloadFinished() {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.onVpaDownload();
      }
    }).start();
  }

  public void notifyJobFinished(JobDescriptor descriptor) {
    JobType type = descriptor.getJobType();
    switch (type) {
      case VPA_EXPORT: {
        String uuid = descriptor.getUuid();
        notifyVpaExport(uuid);
        return;
      }
      case VPA_DOWNLOAD: {
        notifyVpaDownloadFinished();
        return;
      }
      default: {
        throw new UnsupportedOperationException("invalid job type " + type);
      }
    }
  }
}
