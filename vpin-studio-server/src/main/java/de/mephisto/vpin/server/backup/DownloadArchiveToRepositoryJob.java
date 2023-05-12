package de.mephisto.vpin.server.backup;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.Job;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DownloadArchiveToRepositoryJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(DownloadArchiveToRepositoryJob.class);

  private final ArchiveService archiveService;
  private final SystemService systemService;
  private final ArchiveDescriptor archiveDescriptor;

  private String status;

  private File temp;

  public DownloadArchiveToRepositoryJob(@NonNull ArchiveService archiveService,
                                        @NonNull SystemService systemService,
                                        @NonNull ArchiveDescriptor archiveDescriptor) {
    this.archiveService = archiveService;
    this.systemService = systemService;
    this.archiveDescriptor = archiveDescriptor;
  }

  @Override
  public boolean execute() {
    try {
      status = "Downloading " + archiveDescriptor.getFilename();
      File targetFolder = systemService.getVpaArchiveFolder();
      File target = new File(targetFolder, archiveDescriptor.getFilename());
      target = FileUtils.uniqueFile(target);
      temp = new File(target.getParentFile(), target.getName() + ".bak");
      LOG.info("Writing into temporary download file " + temp.getAbsolutePath());
      ArchiveSourceAdapterHttpServer source = (ArchiveSourceAdapterHttpServer) archiveService.getArchiveSourceAdapter(archiveDescriptor.getSource().getId());
      source.download(archiveDescriptor, temp);
      LOG.info("Finished downloading " + archiveDescriptor.getFilename());
      boolean renamed = temp.renameTo(target);
      if (!renamed) {
        LOG.error("Failed to rename downloaded file " + temp.getAbsolutePath());
        return false;
      }
    } catch (Exception e) {
      LOG.error("Download of \"" + archiveDescriptor.getFilename() + "\" failed: " + e.getMessage(), e);
      return false;
    }
    return true;
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
