package de.mephisto.vpin.server.vpxz;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface VPXZSourceAdapter {

  Collection<VPXZDescriptor> getVPXZDescriptors();

  boolean delete(VPXZDescriptor descriptor);

  void invalidate();

  InputStream getVPXMobileInputStream(VPXZDescriptor vpxzDescriptor) throws IOException;

  VPXZSource getVPXZSource();

  File export(VPXZDescriptor VPXZDescriptor);
}
