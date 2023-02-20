package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.restclient.VpaManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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

  public List<VpaDescriptor> getDescriptors() {
    if (cache.isEmpty()) {
      File[] vpaFiles = vpaArchiveFolder.listFiles((dir, name) -> name.endsWith(".vpa"));
      if (vpaFiles != null) {
        for (File vpaFile : vpaFiles) {
          VpaManifest manifest = VpaUtil.readManifest(vpaFile);
          VpaDescriptor descriptor = new VpaDescriptor(source, manifest, new Date(vpaFile.lastModified()),
              vpaFile.getName(), vpaFile.length());
          cache.put(vpaFile.getName(), descriptor);
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
    boolean result =  file.delete();
    this.invalidate();
    return result;
  }

  @Override
  public File getFile(VpaDescriptor descriptor) {
    return new File(vpaArchiveFolder, descriptor.getFilename());
  }

  @Override
  public void invalidate() {
    cache.clear();
    LOG.info("Invalidated VPA source \"" + this.getVpaSource() + "\"");
  }
}
