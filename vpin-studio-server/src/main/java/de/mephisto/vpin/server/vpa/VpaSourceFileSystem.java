package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.commons.VpaSourceType;
import de.mephisto.vpin.restclient.VpaManifest;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VpaSourceFileSystem implements VpaSource {

  private final File vpaArchiveFolder;

  public VpaSourceFileSystem(File vpaArchiveFolder) {
    this.vpaArchiveFolder = vpaArchiveFolder;
  }

  @Override
  public VpaSourceType getType() {
    return VpaSourceType.File;
  }

  public List<VpaDescriptor> getDescriptors() {
    List<VpaDescriptor> descriptors = new ArrayList<>();
    File[] vpaFiles = vpaArchiveFolder.listFiles((dir, name) -> name.endsWith(".vpa"));
    if (vpaFiles != null) {
      for (File vpaFile : vpaFiles) {
        VpaManifest manifest = VpaUtil.readManifest(vpaFile);
        VpaDescriptor descriptor = new VpaDescriptor(this, manifest, new Date(vpaFile.lastModified()),
            FilenameUtils.getBaseName(vpaFile.getName()), vpaFile.length());
        descriptors.add(descriptor);
      }
    }
    return descriptors;
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
}
