package de.mephisto.vpin.server.backup.adapters.vpinzip;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.ArchivePackageInfo;
import de.mephisto.vpin.restclient.TableDetails;
import de.mephisto.vpin.server.backup.ArchiveDescriptor;
import de.mephisto.vpin.server.backup.ArchiveSource;
import de.mephisto.vpin.server.backup.ArchiveSourceAdapter;
import de.mephisto.vpin.server.backup.ArchiveUtil;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class VpinzipArchiveSourceAdapter implements ArchiveSourceAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(VpinzipArchiveSourceAdapter.class);

  private final ArchiveSource source;
  private final File archiveFolder;
  private final Map<String, ArchiveDescriptor> cache = new HashMap<>();

  public VpinzipArchiveSourceAdapter(ArchiveSource source) {
    this.source = source;
    this.archiveFolder = new File(source.getLocation());
    LOG.info("Created Vpinzip archive adapter for folder " + this.archiveFolder.getAbsolutePath());
  }

  public File getFolder() {
    return archiveFolder;
  }

  public List<ArchiveDescriptor> getArchiveDescriptors() {
    if (cache.isEmpty()) {
      File[] archiveFiles = archiveFolder.listFiles((dir, name) -> name.endsWith(".vpinzip"));
      if (archiveFiles != null) {
        for (File archiveFile : archiveFiles) {
          try {
            ArchiveDescriptor archiveDescriptor = ArchiveUtil.readArchiveDescriptor(archiveFile);
            if (archiveDescriptor == null) {
              TableDetails manifest = VpinzipArchiveUtil.readTableDetails(archiveFile);
              ArchivePackageInfo packageInfo = VpinzipArchiveUtil.generatePackageInfo(archiveFile, null);
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

    try {
      List<String> commands = Arrays.asList("vPinBackupManager.exe", "-g");
      LOG.info("Executing refresh command: " + String.join(" ", commands));
      File dir = new File(SystemService.RESOURCES, VpinzipArchiveSource.FOLDER_NAME);
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(dir);
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("Vpinzip Command Error:\n" + standardErrorFromCommand);
      }
      if (!StringUtils.isEmpty(standardOutputFromCommand.toString())) {
        LOG.info("Vpinzip Command StdOut:\n" + standardOutputFromCommand);
      }
      executor.executeCommand();
    } catch (Exception e) {
      LOG.error("Failed to re-generate VPBM manifests: " + e.getMessage(), e);
    }

    LOG.info("Invalidated archive source \"" + this.getArchiveSource() + "\"");
    ArchiveUtil.exportDescriptorJson(this);
  }
}
