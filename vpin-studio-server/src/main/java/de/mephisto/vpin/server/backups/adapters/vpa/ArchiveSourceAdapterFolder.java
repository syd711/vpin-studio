package de.mephisto.vpin.server.backups.adapters.vpa;

import de.mephisto.vpin.restclient.backups.BackupPackageInfo;
import de.mephisto.vpin.restclient.backups.BackupType;
import de.mephisto.vpin.restclient.backups.VpaArchiveUtil;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.server.backups.ArchiveDescriptor;
import de.mephisto.vpin.server.backups.ArchiveSource;
import de.mephisto.vpin.server.backups.ArchiveSourceAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class ArchiveSourceAdapterFolder implements ArchiveSourceAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(ArchiveSourceAdapterFolder.class);

  private final VpaService vpaService;
  private final ArchiveSource source;
  private final File archiveFolder;
  private final Map<String, ArchiveDescriptor> cache = new HashMap<>();

  public ArchiveSourceAdapterFolder(VpaService vpaService, ArchiveSource source) {
    this.vpaService = vpaService;
    this.source = source;
    this.archiveFolder = new File(source.getLocation());
  }

  @Override
  public File export(ArchiveDescriptor archiveDescriptor) {
    Optional<ArchiveDescriptor> first = getArchiveDescriptors().stream().filter(d -> d.getFilename().equals(archiveDescriptor.getFilename())).findFirst();
    if (first.isPresent()) {
      return new File(archiveFolder, archiveDescriptor.getFilename());
    }
    return null;
  }

  public File getFolder() {
    return archiveFolder;
  }

  public List<ArchiveDescriptor> getArchiveDescriptors() {
    if (cache.isEmpty()) {
      File[] vpaFiles = archiveFolder.listFiles((dir, name) -> name.endsWith("." + BackupType.VPA.name().toLowerCase()));
      if (vpaFiles != null) {
        for (File archiveFile : vpaFiles) {
          try {
            TableDetails manifest = VpaArchiveUtil.readTableDetails(archiveFile);
            BackupPackageInfo packageInfo = VpaArchiveUtil.readPackageInfo(archiveFile);
            ArchiveDescriptor descriptor = new ArchiveDescriptor(source, manifest, packageInfo, new Date(archiveFile.lastModified()), archiveFile.getName(), archiveFile.getAbsolutePath(), archiveFile.length());
            cache.put(archiveFile.getName(), descriptor);
          }
          catch (Exception e) {
            LOG.error("Failed to read " + archiveFile.getAbsolutePath() + ": " + e.getMessage());
          }
        }
      }
    }
    return new ArrayList<>(cache.values());
  }

  public ArchiveSource getArchiveSource() {
    return source;
  }

  @Override
  public boolean delete(ArchiveDescriptor descriptor) {
    File file = new File(archiveFolder, descriptor.getFilename());
    LOG.info("Deleting {}", file.getAbsolutePath());
    if (!Desktop.getDesktop().moveToTrash(file)) {
      LOG.error("Failed moving file to trash: " + file.getAbsolutePath());
      if (!file.delete()) {
        LOG.error("Failed to delete " + file.getAbsolutePath());
        return false;
      }
    }
    this.invalidate();
    return true;
  }

  @Override
  public FileInputStream getArchiveInputStream(ArchiveDescriptor archiveDescriptor) throws IOException {
    File file = new File(archiveFolder, archiveDescriptor.getFilename());
    return new FileInputStream(file);
  }

  @Override
  public void invalidate() {
    cache.clear();
    getArchiveDescriptors();
    LOG.info("Invalidated archive source \"" + this.getArchiveSource() + "\"");
  }
}
