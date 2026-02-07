package de.mephisto.vpin.server.vpxz;

import de.mephisto.vpin.restclient.vpxz.VPXZPackageInfo;
import de.mephisto.vpin.restclient.vpxz.VPXZType;
import de.mephisto.vpin.restclient.vpxz.VpxzArchiveUtil;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class VPXZSourceAdapterFolder implements VPXZSourceAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(VPXZSourceAdapterFolder.class);

  private final VPXZSource source;
  private final File archiveFolder;
  private final List<VPXZDescriptor> cache = new ArrayList<>();

  public VPXZSourceAdapterFolder(VPXZSource source) {
    this.source = source;
    this.archiveFolder = new File(source.getLocation());
  }

  @Override
  public File export(VPXZDescriptor VPXZDescriptor) {
    Optional<VPXZDescriptor> first = getVPXZDescriptors().stream().filter(d -> d.getFilename().equals(VPXZDescriptor.getFilename())).findFirst();
    if (first.isPresent()) {
      return new File(archiveFolder, VPXZDescriptor.getFilename());
    }
    return null;
  }

  public File getFolder() {
    return archiveFolder;
  }

  public synchronized Collection<VPXZDescriptor> getVPXZDescriptors() {
    if (cache.isEmpty()) {
      long start = System.currentTimeMillis();
      File[] vpaFiles = archiveFolder.listFiles((dir, name) -> name.endsWith("." + VPXZType.VPXZ.name().toLowerCase()));
      if (vpaFiles != null) {
        for (File archiveFile : vpaFiles) {
          try {
            TableDetails manifest = VpxzArchiveUtil.readTableDetails(archiveFile);
            if(manifest != null) {
              VPXZPackageInfo packageInfo = VpxzArchiveUtil.readPackageInfo(archiveFile);
              VPXZDescriptor descriptor = new VPXZDescriptor(source, manifest, packageInfo, new Date(archiveFile.lastModified()), archiveFile.getName(), archiveFile.getAbsolutePath(), archiveFile.length());
              cache.add(descriptor);
            }
          }
          catch (Exception e) {
            LOG.error("Failed to read " + archiveFile.getAbsolutePath() + ": " + e.getMessage(), e);
          }
        }
        if (!cache.isEmpty()) {
          LOG.info("Loaded existing vpxz: {}, took " + (System.currentTimeMillis() - start) + "ms.", vpaFiles.length);
        }
      }
    }
    return cache;
  }

  public VPXZSource getVPXZSource() {
    return source;
  }

  @Override
  public boolean delete(VPXZDescriptor descriptor) {
    File file = new File(archiveFolder, descriptor.getFilename());
    LOG.info("Deleting {}", file.getAbsolutePath());
    if (file.exists() && !Desktop.getDesktop().moveToTrash(file)) {
      LOG.error("Failed moving file to trash: " + file.getAbsolutePath());
      if (!file.delete()) {
        LOG.error("Failed to delete " + file.getAbsolutePath());
        return false;
      }
    }
    else {
      this.cache.remove(descriptor);
    }
    return true;
  }

  @Override
  public FileInputStream getVPXMobileInputStream(VPXZDescriptor vpxzDescriptor) throws IOException {
    File file = new File(archiveFolder, vpxzDescriptor.getFilename());
    return new FileInputStream(file);
  }

  @Override
  public void invalidate() {
    cache.clear();
  }
}
