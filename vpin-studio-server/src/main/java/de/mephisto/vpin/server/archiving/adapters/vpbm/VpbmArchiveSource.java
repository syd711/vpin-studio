package de.mephisto.vpin.server.archiving.adapters.vpbm;

import de.mephisto.vpin.commons.ArchiveSourceType;
import de.mephisto.vpin.server.archiving.ArchiveSource;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;
import java.util.Date;

public class VpbmArchiveSource extends ArchiveSource {
  private final static long DEFAULT_ARCHIVE_SOURCE_ID = -2;
  public final static String FOLDER_NAME = "vpbm";

  private final File folder;

  public VpbmArchiveSource(@NonNull File folder) {
    this.folder = folder;
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
    return new Date(folder.lastModified());
  }

  @Override
  public Long getId() {
    return DEFAULT_ARCHIVE_SOURCE_ID;
  }

  @Override
  public String getName() {
    return "VPBM Repository";
  }

  @Override
  public String getLocation() {
    return folder.getAbsolutePath();
  }
}
