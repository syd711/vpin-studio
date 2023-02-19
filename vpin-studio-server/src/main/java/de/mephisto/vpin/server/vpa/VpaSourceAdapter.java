package de.mephisto.vpin.server.vpa;

import java.io.File;
import java.util.List;

public interface VpaSourceAdapter {

  List<VpaDescriptor> getDescriptors();

  boolean delete(VpaDescriptor descriptor);

  void invalidate();

  File getFile(VpaDescriptor vpaDescriptor);

  VpaSource getVpaSource();
}
