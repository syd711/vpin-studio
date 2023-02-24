package de.mephisto.vpin.server.vpa;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface VpaSourceAdapter {

  List<VpaDescriptor> getVpaDescriptors();

  boolean delete(VpaDescriptor descriptor);

  void invalidate();

  InputStream getDescriptorInputStream(VpaDescriptor vpaDescriptor) throws IOException;

  VpaSource getVpaSource();
}
