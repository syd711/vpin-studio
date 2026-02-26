package de.mephisto.vpin.server.vpxz;

import de.mephisto.vpin.restclient.vpxz.VPXZSourceType;
import de.mephisto.vpin.restclient.vpxz.VPXZType;
import de.mephisto.vpin.server.backups.BackupSource;
import de.mephisto.vpin.server.system.SystemService;

import java.io.File;
import java.util.Date;

public class VPXZSourceImpl extends VPXZSource {
  public final static long DEFAULT_VPXZ_SOURCE_ID = -1;

  public final static File FOLDER = new File(SystemService.RESOURCES, VPXZType.VPXZ.name().toLowerCase() + "/");

  static {
    if (!FOLDER.exists()) {
      FOLDER.mkdirs();
    }
  }

  public VPXZSourceImpl() {

  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public String getType() {
    return VPXZSourceType.Folder.name();
  }

  @Override
  public Date getCreatedAt() {
    return new Date(FOLDER.lastModified());
  }

  @Override
  public Long getId() {
    return DEFAULT_VPXZ_SOURCE_ID;
  }

  @Override
  public String getName() {
    return "Default VPXZ Folder";
  }

  @Override
  public String getLocation() {
    return FOLDER.getAbsolutePath();
  }
}
