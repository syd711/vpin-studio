package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.restclient.VpaManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class VpaSourceAdapterFileSystem implements VpaSourceAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(VpaSourceAdapterFileSystem.class);

  private final VpaSource source;
  private final File vpaArchiveFolder;
  private final Map<String, VpaDescriptor> cache = new HashMap<>();

  public VpaSourceAdapterFileSystem(VpaSource source) {
    this.source = source;
    this.vpaArchiveFolder = new File(source.getLocation());
  }

  public File getFolder() {
    return vpaArchiveFolder;
  }

  public List<VpaDescriptor> getVpaDescriptors() {
    if (cache.isEmpty()) {
      File[] vpaFiles = vpaArchiveFolder.listFiles((dir, name) -> name.endsWith(".vpa"));
      if (vpaFiles != null) {
        for (File vpaFile : vpaFiles) {
          try {
            VpaManifest manifest = VpaUtil.readManifest(vpaFile);
            VpaDescriptor descriptor = new VpaDescriptor(source, manifest, new Date(vpaFile.lastModified()),
                vpaFile.getName(), vpaFile.length());
            manifest.setVpaFileSize(vpaFile.length());
            cache.put(vpaFile.getName(), descriptor);
          } catch (Exception e) {
            LOG.error("Failed to read " + vpaFile.getAbsolutePath() + ": " + e.getMessage(), e);
          }
        }
      }
    }
    return new ArrayList<>(cache.values());
  }

  public VpaSource getVpaSource() {
    return source;
  }

  @Override
  public boolean delete(VpaDescriptor descriptor) {
    File file = new File(vpaArchiveFolder, descriptor.getFilename());
    if (!file.delete()) {
      LOG.error("Failed to delete " + file.getAbsolutePath());
      return false;
    }
    this.invalidate();
    return true;
  }

  @Override
  public FileInputStream getDescriptorInputStream(VpaDescriptor descriptor) throws IOException {
    File file = new File(vpaArchiveFolder, descriptor.getFilename());
    return new FileInputStream(file);
  }

  @Override
  public void invalidate() {
    cache.clear();
    LOG.info("Invalidated VPA source \"" + this.getVpaSource() + "\"");

    if (this.getVpaSource().getId() == -1) {
      VpaUtil.exportDescriptorJson(this);
    }
  }
}
