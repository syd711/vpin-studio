package de.mephisto.vpin.server.backups.adapters.vpa;

import de.mephisto.vpin.restclient.backups.BackupSourceType;
import de.mephisto.vpin.server.backups.BackupSource;
import de.mephisto.vpin.server.system.SystemService;

import java.io.File;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class VpaBackupSource extends BackupSource {
  public final static long DEFAULT_ARCHIVE_SOURCE_ID = -1;

  public final static File FOLDER = new File(SystemService.RESOURCES, "vpa/");

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
  public OffsetDateTime getCreatedAt() {
    return OffsetDateTime.ofInstant(Instant.ofEpochMilli(FOLDER.lastModified()), ZoneId.systemDefault());
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
