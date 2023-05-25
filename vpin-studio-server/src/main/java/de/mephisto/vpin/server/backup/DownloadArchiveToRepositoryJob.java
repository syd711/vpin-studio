package de.mephisto.vpin.server.backup;

import de.mephisto.vpin.restclient.Job;
import de.mephisto.vpin.restclient.JobExecutionResult;
import de.mephisto.vpin.restclient.JobExecutionResultFactory;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DownloadArchiveToRepositoryJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(DownloadArchiveToRepositoryJob.class);

  private final ArchiveService archiveService;
  private final ArchiveDescriptor archiveDescriptor;

  private String status;

  private File temp;

  public DownloadArchiveToRepositoryJob(@NonNull ArchiveService archiveService,
                                        @NonNull ArchiveDescriptor archiveDescriptor) {
    this.archiveService = archiveService;
    this.archiveDescriptor = archiveDescriptor;
  }

  @Override
  public JobExecutionResult execute() {
    try {
      status = "Downloading " + archiveDescriptor.getFilename();
      File archiveTarget = archiveService.getTargetFile(archiveDescriptor);
      temp = new File(archiveTarget.getParentFile(), archiveDescriptor.getFilename() + ".bak");
      if (temp.exists()) {
        temp.delete();
      }
      if (archiveTarget.exists()) {
        archiveTarget.delete();
      }

      LOG.info("Writing into temporary download file " + temp.getAbsolutePath());
      ArchiveSourceAdapterHttpServer source = (ArchiveSourceAdapterHttpServer) archiveService.getArchiveSourceAdapter(archiveDescriptor.getSource().getId());
      source.downloadArchive(archiveDescriptor, temp);
      LOG.info("Finished downloading " + archiveDescriptor.getFilename());
      boolean renamed = temp.renameTo(archiveTarget);
      if (!renamed) {
        LOG.error("Failed to rename downloaded file " + temp.getAbsolutePath());
        return JobExecutionResultFactory.create("Failed to rename downloaded file " + temp.getAbsolutePath());
      }

      File descriptorTarget = new File(archiveTarget.getParentFile(), FilenameUtils.getBaseName(archiveDescriptor.getFilename()) + ".json");
      temp = new File(descriptorTarget.getParentFile(), descriptorTarget.getName() + ".bak");
      if (temp.exists()) {
        temp.delete();
      }
      if (descriptorTarget.exists()) {
        descriptorTarget.delete();
      }

      source.downloadDescriptor(archiveDescriptor, temp);
      LOG.info("Finished downloading " + descriptorTarget.getAbsolutePath());
      renamed = temp.renameTo(descriptorTarget);
      if (!renamed) {
        LOG.error("Failed to rename downloaded file " + temp.getAbsolutePath());
        return JobExecutionResultFactory.create("Failed to rename downloaded file " + temp.getAbsolutePath());
      }

    } catch (Exception e) {
      LOG.error("Download of \"" + archiveDescriptor.getFilename() + "\" failed: " + e.getMessage(), e);
      return JobExecutionResultFactory.create("Download of \"" + archiveDescriptor.getFilename() + "\" failed: " + e.getMessage());
    }
    return new JobExecutionResult();
  }

  @Override
  public double getProgress() {
    long currentSize = temp.length();
    return currentSize * 100d / archiveDescriptor.getSize();
  }

  @Override
  public String getStatus() {
    return status;
  }
}
