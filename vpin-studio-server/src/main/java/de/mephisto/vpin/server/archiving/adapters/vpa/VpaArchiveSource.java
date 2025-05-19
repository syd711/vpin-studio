package de.mephisto.vpin.server.archiving.adapters.vpa;

import de.mephisto.vpin.commons.ArchiveSourceType;
import de.mephisto.vpin.server.archiving.ArchiveSource;
import de.mephisto.vpin.server.system.SystemService;

import java.io.File;
import java.util.Date;

public class VpaArchiveSource extends ArchiveSource {
  public final static long DEFAULT_ARCHIVE_SOURCE_ID = -1;

  public final static File FOLDER = new File(SystemService.RESOURCES, "vpa/");

  static {
    if(!FOLDER.exists()) {
      FOLDER.mkdirs();
    }
  }

  public VpaArchiveSource() {

  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public String getType() {
    return ArchiveSourceType.File.name();
  }

  @Override
  public Date getCreatedAt() {
    return new Date(FOLDER.lastModified());
  }

  @Override
  public Long getId() {
    return DEFAULT_ARCHIVE_SOURCE_ID;
  }

  @Override
  public String getName() {
    return "VPA Repository";
  }

  @Override
  public String getLocation() {
    return FOLDER.getAbsolutePath();
  }
}
