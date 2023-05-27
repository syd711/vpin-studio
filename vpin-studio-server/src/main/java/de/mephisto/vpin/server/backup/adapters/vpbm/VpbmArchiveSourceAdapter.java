package de.mephisto.vpin.server.backup.adapters.vpbm;

import de.mephisto.vpin.restclient.ArchivePackageInfo;
import de.mephisto.vpin.restclient.TableDetails;
import de.mephisto.vpin.server.backup.ArchiveDescriptor;
import de.mephisto.vpin.server.backup.ArchiveSource;
import de.mephisto.vpin.server.backup.ArchiveSourceAdapter;
import de.mephisto.vpin.server.backup.ArchiveUtil;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class VpbmArchiveSourceAdapter implements ArchiveSourceAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(VpbmArchiveSourceAdapter.class);

  private final ArchiveSource source;
  private final File archiveFolder;
  private final VpbmService vpbmService;
  private final Map<String, ArchiveDescriptor> cache = new HashMap<>();

  public VpbmArchiveSourceAdapter(ArchiveSource source, VpbmService vpbmService) {
    this.source = source;
    this.archiveFolder = new File(source.getLocation());
    this.vpbmService = vpbmService;
  }

  public File getFolder() {
    return archiveFolder;
  }

  @Override
  public File export(ArchiveDescriptor archiveDescriptor) {
    return vpbmService.export(archiveDescriptor.getFilename());
  }

  public List<ArchiveDescriptor> getArchiveDescriptors() {
    if (cache.isEmpty()) {
      File[] archiveFiles = archiveFolder.listFiles((dir, name) -> name.endsWith(".vpinzip"));
      if (archiveFiles != null) {
        for (File archiveFile : archiveFiles) {
          try {
            ArchiveDescriptor archiveDescriptor = ArchiveUtil.readArchiveDescriptor(source, archiveFile);
            if (archiveDescriptor == null) {
              TableDetails manifest = VpbmArchiveUtil.readTableDetails(archiveFile);
              ArchivePackageInfo packageInfo = VpbmArchiveUtil.generatePackageInfo(archiveFile, null);
              archiveDescriptor = new ArchiveDescriptor(source, manifest, packageInfo, new Date(archiveFile.lastModified()), archiveFile.getName(), archiveFile.length());
            }

            cache.put(archiveFile.getName(), archiveDescriptor);
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
    boolean result = true;
    if (!file.delete()) {
      LOG.error("Failed to delete " + file.getAbsolutePath());
      result = false;
    }

    File descriptorFile = new File(archiveFolder, FilenameUtils.getBaseName(descriptor.getFilename()) + ".json");
    if (!descriptorFile.delete()) {
      LOG.error("Failed to delete " + descriptorFile.getAbsolutePath());
      result = false;
    }

    this.invalidate();
    return result;
  }

  @Override
  public FileInputStream getArchiveInputStream(ArchiveDescriptor archiveDescriptor) throws IOException {
    File file = new File(archiveFolder, archiveDescriptor.getFilename());
    return new FileInputStream(file);
  }

  @Override
  public void invalidate() {
    cache.clear();
    LOG.info("Invalidated archive source \"" + this.getArchiveSource() + "\"");
    ArchiveUtil.exportDescriptorJson(this);
  }
}
