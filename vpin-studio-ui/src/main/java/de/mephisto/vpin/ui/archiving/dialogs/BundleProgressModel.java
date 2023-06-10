package de.mephisto.vpin.ui.archiving.dialogs;

import de.mephisto.vpin.restclient.descriptors.ArchiveBundleDescriptor;
import de.mephisto.vpin.restclient.descriptors.DownloadJobDescriptor;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static de.mephisto.vpin.ui.Studio.client;

public class BundleProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(BundleProgressModel.class);
  private final File targetFolder;
  private final ArchiveBundleDescriptor archiveBundleDescriptor;
  private boolean hasNext = true;

  public BundleProgressModel(String title, File targetFolder, ArchiveBundleDescriptor archiveBundleDescriptor) {
    super(title);
    this.targetFolder = targetFolder;
    this.archiveBundleDescriptor = archiveBundleDescriptor;
  }

  @Override
  public int getMax() {
    return 1;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public String getNext() {
    return "Creating bundle archive, download will start afterwards.";
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public String nextToString(String next) {
    return next;
  }

  @Override
  public boolean hasNext() {
    return hasNext;
  }

  public void processNext(ProgressResultModel progressResultModel, String item) {
    try {
      String bundleName = client.getArchiveService().bundle(archiveBundleDescriptor);
      hasNext = false;

      Platform.runLater(() -> {
        File target = new File(targetFolder, bundleName);
        DownloadJobDescriptor job = new DownloadJobDescriptor("archives/download/bundle/" + archiveBundleDescriptor.getArchiveSourceId() + "/" + URLEncoder.encode(bundleName, StandardCharsets.UTF_8), target);
        job.setTitle("Download of \"" + bundleName + "\"");
        job.setDescription("Downloading file \"" + bundleName + "\"");
        JobPoller.getInstance().queueJob(job);
      });
    } catch (Exception e) {
      LOG.error("Generate bundle download error: " + e.getMessage(), e);
    }
  }
}
