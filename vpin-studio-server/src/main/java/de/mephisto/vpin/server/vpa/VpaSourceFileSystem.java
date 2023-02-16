package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.commons.VpaSourceType;
import de.mephisto.vpin.restclient.VpaManifest;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class VpaSourceFileSystem implements VpaSource {
  private final static Logger LOG = LoggerFactory.getLogger(VpaSourceFileSystem.class);

  private final File vpaArchiveFolder;
  private final Map<String, VpaDescriptor> cache = new HashMap<>();

  public VpaSourceFileSystem(File vpaArchiveFolder) {
    this.vpaArchiveFolder = vpaArchiveFolder;
  }

  @Override
  public VpaSourceType getType() {
    return VpaSourceType.File;
  }

  public List<VpaDescriptor> getDescriptors() {
    if (cache.isEmpty()) {
      File[] vpaFiles = vpaArchiveFolder.listFiles((dir, name) -> name.endsWith(".vpa"));
      if (vpaFiles != null) {
        for (File vpaFile : vpaFiles) {
          VpaManifest manifest = VpaUtil.readManifest(vpaFile);
          VpaDescriptor descriptor = new VpaDescriptor(this, manifest, new Date(vpaFile.lastModified()),
              FilenameUtils.getBaseName(vpaFile.getName()), vpaFile.length());
          cache.put(vpaFile.getName(), descriptor);
        }
      }
    }
    return new ArrayList<>(cache.values());
  }

  @Override
  public boolean delete(VpaDescriptor descriptor) {
    File file = new File(vpaArchiveFolder, descriptor.getName() + ".vpa");
    return file.delete();
  }

  @Override
  public String getLocation() {
    return vpaArchiveFolder.getAbsolutePath();
  }

  @Override
  public void invalidate() {
    cache.clear();
    LOG.info("Invalidated VPA source \"" + this.getLocation() + "\"");
  }
}
