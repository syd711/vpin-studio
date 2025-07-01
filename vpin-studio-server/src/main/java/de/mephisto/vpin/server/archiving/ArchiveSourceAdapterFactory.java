package de.mephisto.vpin.server.archiving;

import de.mephisto.vpin.commons.ArchiveSourceType;
import de.mephisto.vpin.server.archiving.adapters.vpa.ArchiveSourceAdapterFolder;

public class ArchiveSourceAdapterFactory {

  public static ArchiveSourceAdapter create(ArchiveService archiveService, ArchiveSource source) {
    ArchiveSourceType archiveSourceType = ArchiveSourceType.valueOf(source.getType());
    switch (archiveSourceType) {
      case Folder: {
        return new ArchiveSourceAdapterFolder(source);
      }
      case Http: {
        return new ArchiveSourceAdapterHttpServer(archiveService, source);
      }
      default: {
        throw new UnsupportedOperationException("Invalid source type: " + archiveSourceType);
      }
    }
  }
}
