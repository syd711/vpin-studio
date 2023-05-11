package de.mephisto.vpin.ui.events;

import de.mephisto.vpin.restclient.descriptors.JobDescriptor;
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

  public void notifyTableBackedUp() {
    new Thread(() -> {
      TableBackedUpEvent event = new TableBackedUpEvent();
      for (StudioEventListener listener : listeners) {
        listener.onTableBackedUp(event);
      }
    }).start();
  }

  public void notifyArchiveInstallation() {
    new Thread(() -> {
      ArchiveInstalledEvent event = new ArchiveInstalledEvent();
      for (StudioEventListener listener : listeners) {
        listener.onArchiveInstalled(event);
      }
    }).start();
  }

  public void notifyArchiveSourceUpdate() {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.onArchiveSourceUpdate();
      }
    }).start();
  }

  public void notifyArchiveCopyFinished() {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.onArchiveCopiedToRepository();
      }
    }).start();
  }

  public void notifyArchiveDownloadFinished() {
    new Thread(() -> {
      for (StudioEventListener listener : listeners) {
        listener.onArchiveDownload();
      }
    }).start();
  }

  public void notifyJobFinished(JobDescriptor descriptor) {
    JobType type = descriptor.getJobType();
    switch (type) {
      case TABLE_BACKUP: {
        notifyTableBackedUp();
        return;
      }
      case ARCHIVE_INSTALL: {
        notifyArchiveInstallation();
        return;
      }
      case ARCHIVE_DOWNLOAD_TO_REPOSITORY: {
        notifyArchiveCopyFinished();
        return;
      }
      case ARCHIVE_DOWNLOAD_TO_FILESYSTEM: {
        notifyArchiveDownloadFinished();
        return;
      }
      default: {
        throw new UnsupportedOperationException("invalid job type " + type);
      }
    }
  }
}
