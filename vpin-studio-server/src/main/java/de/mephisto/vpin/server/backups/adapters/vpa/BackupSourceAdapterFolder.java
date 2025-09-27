package de.mephisto.vpin.server.backups.adapters.vpa;

import de.mephisto.vpin.restclient.backups.BackupPackageInfo;
import de.mephisto.vpin.restclient.backups.BackupType;
import de.mephisto.vpin.restclient.backups.VpaArchiveUtil;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.server.backups.BackupDescriptor;
import de.mephisto.vpin.server.backups.BackupSource;
import de.mephisto.vpin.server.backups.BackupSourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class BackupSourceAdapterFolder implements BackupSourceAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(BackupSourceAdapterFolder.class);

  private final BackupSource source;
  private final File archiveFolder;
  private final Map<String, BackupDescriptor> cache = new HashMap<>();

  public BackupSourceAdapterFolder(BackupSource source) {
    this.source = source;
    this.archiveFolder = new File(source.getLocation());
  }

  @Override
  public File export(BackupDescriptor backupDescriptor) {
    Optional<BackupDescriptor> first = getBackupDescriptors().stream().filter(d -> d.getFilename().equals(backupDescriptor.getFilename())).findFirst();
    if (first.isPresent()) {
      return new File(archiveFolder, backupDescriptor.getFilename());
    }
    return null;
  }

  public File getFolder() {
    return archiveFolder;
  }

  public List<BackupDescriptor> getBackupDescriptors() {
    if (cache.isEmpty()) {
      File[] vpaFiles = archiveFolder.listFiles((dir, name) -> name.endsWith("." + BackupType.VPA.name().toLowerCase()));
      if (vpaFiles != null) {
        for (File archiveFile : vpaFiles) {
          try {
            TableDetails manifest = VpaArchiveUtil.readTableDetails(archiveFile);
            BackupPackageInfo packageInfo = VpaArchiveUtil.readPackageInfo(archiveFile);
            BackupDescriptor descriptor = new BackupDescriptor(source, manifest, packageInfo, new Date(archiveFile.lastModified()), archiveFile.getName(), archiveFile.getAbsolutePath(), archiveFile.length());
            cache.put(archiveFile.getName(), descriptor);
          }
          catch (Exception e) {
            LOG.error("Failed to read " + archiveFile.getAbsolutePath() + ": " + e.getMessage(), e);
          }
        }
      }
    }
    return new ArrayList<>(cache.values());
  }

  public BackupSource getBackupSource() {
    return source;
  }

  @Override
  public boolean delete(BackupDescriptor descriptor) {
    File file = new File(archiveFolder, descriptor.getFilename());
    LOG.info("Deleting {}", file.getAbsolutePath());
    if (!Desktop.getDesktop().moveToTrash(file)) {
      LOG.error("Failed moving file to trash: " + file.getAbsolutePath());
      if (!file.delete()) {
        LOG.error("Failed to delete " + file.getAbsolutePath());
        return false;
      }
    }
    else {
      this.cache.remove(file.getName());
    }
    return true;
  }

  @Override
  public FileInputStream getBackupInputStream(BackupDescriptor backupDescriptor) throws IOException {
    File file = new File(archiveFolder, backupDescriptor.getFilename());
    return new FileInputStream(file);
  }

  @Override
  public void invalidate() {
    cache.clear();
    getBackupDescriptors();
    LOG.info("Invalidated archive source \"" + this.getBackupSource() + "\"");
  }
}
