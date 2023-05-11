package de.mephisto.vpin.server.backup;

import de.mephisto.vpin.commons.ArchiveSourceType;

public class ArchiveSourceAdapterFactory {

  public static ArchiveSourceAdapter create(ArchiveSource source) {
    ArchiveSourceType archiveSourceType = ArchiveSourceType.valueOf(source.getType());
    switch (archiveSourceType) {
      case File: {
        return new ArchiveSourceAdapterFileSystem(source);
      }
      case Http: {
        return new ArchiveSourceAdapterHttpServer(source);
      }
      default: {
        throw new UnsupportedOperationException("Invalid source type: " + archiveSourceType);
      }
    }
  }
}
