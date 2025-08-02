package de.mephisto.vpin.server.backups;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ArchiveSourceAdapter {

  List<ArchiveDescriptor> getArchiveDescriptors();

  boolean delete(ArchiveDescriptor descriptor);

  void invalidate();

  InputStream getArchiveInputStream(ArchiveDescriptor archiveDescriptor) throws IOException;

  ArchiveSource getArchiveSource();

  File export(ArchiveDescriptor archiveDescriptor);
}
