package de.mephisto.vpin.server.backup;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ArchiveSourceAdapter {

  List<ArchiveDescriptor> getArchiveDescriptors();

  boolean delete(ArchiveDescriptor descriptor);

  void invalidate();

  InputStream getDescriptorInputStream(ArchiveDescriptor archiveDescriptor) throws IOException;

  ArchiveSource getArchiveSource();
}
