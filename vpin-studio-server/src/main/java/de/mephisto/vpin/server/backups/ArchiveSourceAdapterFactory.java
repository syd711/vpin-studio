package de.mephisto.vpin.server.backups;

import de.mephisto.vpin.commons.BackupSourceType;
import de.mephisto.vpin.server.backups.adapters.vpa.ArchiveSourceAdapterFolder;
import de.mephisto.vpin.server.backups.adapters.vpa.VpaService;

public class ArchiveSourceAdapterFactory {

  public static ArchiveSourceAdapter create(ArchiveService archiveService, ArchiveSource source, VpaService vpaService) {
    BackupSourceType backupSourceType = BackupSourceType.valueOf(source.getType());
    switch (backupSourceType) {
      case Folder: {
        return new ArchiveSourceAdapterFolder(vpaService, source);
      }
      case Http: {
        return new ArchiveSourceAdapterHttpServer(archiveService, source);
      }
      default: {
        throw new UnsupportedOperationException("Invalid source type: " + backupSourceType);
      }
    }
  }
}
