package de.mephisto.vpin.server.archiving;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class CopyArchiveToRepositoryJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(CopyArchiveToRepositoryJob.class);

  private final ArchiveService archiveService;
  private final ArchiveDescriptor archiveDescriptor;
  private final boolean overwrite;

  private File temp;
  private JobDescriptor result;

  public CopyArchiveToRepositoryJob(@NonNull ArchiveService archiveService,
                                    @NonNull ArchiveDescriptor archiveDescriptor,
                                    boolean overwrite) {
    this.archiveService = archiveService;
    this.archiveDescriptor = archiveDescriptor;
    this.overwrite = overwrite;
  }

  @Override
  public void execute(JobDescriptor result) {
    this.result = result;
    try {
      result.setStatus("Downloading " + archiveDescriptor.getFilename());
      File archiveTarget = archiveService.getTargetFile(archiveDescriptor);
      temp = new File(archiveTarget.getParentFile(), archiveDescriptor.getFilename() + ".bak");
      if (temp.exists()) {
        temp.delete();
      }
      if (archiveTarget.exists()) {
        if (overwrite && !archiveTarget.delete()) {
          result.setError("Failed to delete existing archive " + archiveTarget.getAbsolutePath());
          return;
        }

        if (!overwrite) {
          return;
        }
      }

      LOG.info("Writing into temporary download file " + temp.getAbsolutePath());
      ArchiveSourceAdapterHttpServer source = (ArchiveSourceAdapterHttpServer) archiveService.getArchiveSourceAdapter(archiveDescriptor.getSource().getId());
      source.downloadArchive(archiveDescriptor, temp);
      LOG.info("Finished downloading " + archiveDescriptor.getFilename());
      boolean renamed = temp.renameTo(archiveTarget);
      if (!renamed) {
        LOG.error("Failed to rename downloaded file " + temp.getAbsolutePath());
        result.setError("Failed to rename downloaded file " + temp.getAbsolutePath());
        return;
      }

      if (archiveDescriptor.getFilename().endsWith(".vpa")) {
        return;
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
        result.setError("Failed to rename downloaded file " + temp.getAbsolutePath());
      }

    }
    catch (Exception e) {
      LOG.error("Download of \"" + archiveDescriptor.getFilename() + "\" failed: " + e.getMessage(), e);
      result.setError("Download of \"" + archiveDescriptor.getFilename() + "\" failed: " + e.getMessage());
    }
  }

  public void setProgress() {
    long currentSize = temp.length();
    result.setProgress(currentSize * 100d / archiveDescriptor.getSize() / 100);
  }
}
