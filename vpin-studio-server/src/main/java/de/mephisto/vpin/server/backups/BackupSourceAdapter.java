package de.mephisto.vpin.server.backups;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public interface BackupSourceAdapter {

  Collection<BackupDescriptor> getBackupDescriptors();

  boolean delete(BackupDescriptor descriptor);

  void invalidate();

  InputStream getBackupInputStream(BackupDescriptor backupDescriptor) throws IOException;

  BackupSource getBackupSource();

  File export(BackupDescriptor backupDescriptor);
}
