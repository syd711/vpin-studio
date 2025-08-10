package de.mephisto.vpin.ui.backups.dialogs;

import de.mephisto.vpin.restclient.backups.BackupServiceClient;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;

public class BackupUploadProgressModel extends ProgressModel<File> {
  private final static Logger LOG = LoggerFactory.getLogger(BackupUploadProgressModel.class);

  private final Iterator<File> iterator;
  private final long repositoryId;
  private final List<File> files;
  private double percentage = 0;

  private Future<JobDescriptor> currentUploadFuture;
  private final BackupServiceClient backupServiceClient;

  public BackupUploadProgressModel(String title, long repositoryId, List<File> files) {
    super(title);
    this.repositoryId = repositoryId;
    this.files = files;
    this.iterator = files.iterator();
    backupServiceClient = Studio.client.getArchiveService();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public File getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(File file) {
    return "Uploading " + file.getName();
  }

  @Override
  public int getMax() {
    return files.size();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      currentUploadFuture = backupServiceClient.uploadBackupFuture(next, (int) repositoryId, percent -> {
        double total = percentage + percent;
        progressResultModel.setProgress(total / this.files.size());
      });
      currentUploadFuture.get();

      progressResultModel.addProcessed();
      percentage++;
    } catch (Exception e) {
      if (!currentUploadFuture.isCancelled()) {
        LOG.error("Archive upload failed: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public void cancel() {
    if (currentUploadFuture != null && !currentUploadFuture.isDone()) {
      currentUploadFuture.cancel(true);

      LOG.warn("Upload cancelled");
    }
  }
}
