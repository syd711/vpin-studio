package de.mephisto.vpin.server.backups;

import de.mephisto.vpin.commons.BackupSourceType;
import de.mephisto.vpin.server.backups.adapters.vpa.BackupSourceAdapterFolder;
import de.mephisto.vpin.server.backups.adapters.vpa.VpaService;

public class BackupSourceAdapterFactory {

  public static BackupSourceAdapter create(BackupService backupService, BackupSource source, VpaService vpaService) {
    BackupSourceType backupSourceType = BackupSourceType.valueOf(source.getType());
    switch (backupSourceType) {
      case Folder: {
        return new BackupSourceAdapterFolder(vpaService, source);
      }
      case Http: {
        return new BackupSourceAdapterHttpServer(backupService, source);
      }
      default: {
        throw new UnsupportedOperationException("Invalid source type: " + backupSourceType);
      }
    }
  }
}
