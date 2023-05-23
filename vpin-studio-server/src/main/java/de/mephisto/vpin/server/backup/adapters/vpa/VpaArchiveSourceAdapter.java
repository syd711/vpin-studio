package de.mephisto.vpin.server.backup.adapters.vpa;

import de.mephisto.vpin.restclient.ArchivePackageInfo;
import de.mephisto.vpin.restclient.TableDetails;
import de.mephisto.vpin.server.backup.ArchiveDescriptor;
import de.mephisto.vpin.server.backup.ArchiveSource;
import de.mephisto.vpin.server.backup.ArchiveSourceAdapter;
import de.mephisto.vpin.server.backup.ArchiveUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class VpaArchiveSourceAdapter implements ArchiveSourceAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(VpaArchiveSourceAdapter.class);

  private final ArchiveSource source;
  private final File archiveFolder;
  private final Map<String, ArchiveDescriptor> cache = new HashMap<>();

  public VpaArchiveSourceAdapter(ArchiveSource source) {
    this.source = source;
    this.archiveFolder = new File(source.getLocation());
  }

  @Override
  public File export(ArchiveDescriptor archiveDescriptor, boolean overwrite) {
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
      File[] vpaFiles = archiveFolder.listFiles((dir, name) -> name.endsWith(".vpa"));
      if (vpaFiles != null) {
        for (File archiveFile : vpaFiles) {
          try {
            TableDetails manifest = VpaArchiveUtil.readTableDetails(archiveFile);
            ArchivePackageInfo packageInfo = VpaArchiveUtil.readPackageInfo(archiveFile);
            ArchiveDescriptor descriptor = new ArchiveDescriptor(source, manifest, packageInfo, new Date(archiveFile.lastModified()), archiveFile.getName(), archiveFile.length());
            cache.put(archiveFile.getName(), descriptor);
          } catch (Exception e) {
            LOG.error("Failed to read " + archiveFile.getAbsolutePath() + ": " + e.getMessage(), e);
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
    if (!file.delete()) {
      LOG.error("Failed to delete " + file.getAbsolutePath());
      return false;
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
    ArchiveUtil.exportDescriptorJson(this);
    LOG.info("Invalidated archive source \"" + this.getArchiveSource() + "\"");
  }
}
