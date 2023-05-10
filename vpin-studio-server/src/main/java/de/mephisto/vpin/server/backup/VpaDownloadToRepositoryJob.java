package de.mephisto.vpin.server.backup;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.Job;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class VpaDownloadToRepositoryJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(VpaDownloadToRepositoryJob.class);

  private final ArchiveService vpaService;
  private final SystemService systemService;
  private final ArchiveDescriptor vpaDescriptor;

  private String status;

  private File temp;

  public VpaDownloadToRepositoryJob(@NonNull ArchiveService vpaService,
                                    @NonNull SystemService systemService,
                                    @NonNull ArchiveDescriptor vpaDescriptor) {
    this.vpaService = vpaService;
    this.systemService = systemService;
    this.vpaDescriptor = vpaDescriptor;
  }

  @Override
  public boolean execute() {
    try {
      status = "Downloading " + vpaDescriptor.getFilename();
      File targetFolder = systemService.getVpaArchiveFolder();
      File target = new File(targetFolder, vpaDescriptor.getFilename());
      target = FileUtils.uniqueFile(target);
      temp = new File(target.getParentFile(), target.getName() + ".bak");
      LOG.info("Writing into temporary download file " + temp.getAbsolutePath());
      ArchiveSourceAdapterHttpServer source = (ArchiveSourceAdapterHttpServer) vpaService.getVpaSourceAdapter(vpaDescriptor.getSource().getId());
      source.download(vpaDescriptor, temp);
      LOG.info("Finished downloading " + vpaDescriptor.getFilename());
      boolean renamed = temp.renameTo(target);
      if (!renamed) {
        LOG.error("Failed to rename downloaded file " + temp.getAbsolutePath());
        return false;
      }
    } catch (Exception e) {
      LOG.error("Download of \"" + vpaDescriptor.getFilename() + "\" failed: " + e.getMessage(), e);
      return false;
    }
    return true;
  }

  public File getDownloadedFile() {
    return this.temp;
  }

  @Override
  public double getProgress() {
    long currentSize = temp.length();
    return currentSize * 100d / vpaDescriptor.getSize();
  }

  @Override
  public String getStatus() {
    return status;
  }
}
