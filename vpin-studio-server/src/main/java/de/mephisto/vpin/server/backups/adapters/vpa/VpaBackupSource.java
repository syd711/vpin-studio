package de.mephisto.vpin.server.backups.adapters.vpa;

import de.mephisto.vpin.commons.BackupSourceType;
import de.mephisto.vpin.restclient.backups.BackupType;
import de.mephisto.vpin.server.backups.BackupSource;
import de.mephisto.vpin.server.system.SystemService;

import java.io.File;
import java.util.Date;

public class VpaBackupSource extends BackupSource {
  public final static long DEFAULT_ARCHIVE_SOURCE_ID = -1;

  public final static File FOLDER = new File(SystemService.RESOURCES, BackupType.VPA.name().toLowerCase() + "/");

  static {
    if (!FOLDER.exists()) {
      FOLDER.mkdirs();
    }
  }

  public VpaBackupSource() {

  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public String getType() {
    return BackupSourceType.Folder.name();
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
    return "Default Backups Folder";
  }

  @Override
  public String getLocation() {
    return FOLDER.getAbsolutePath();
  }
}
