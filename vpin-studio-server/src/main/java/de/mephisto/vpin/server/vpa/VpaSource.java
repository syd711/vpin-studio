package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.commons.VpaSourceType;

import java.io.File;
import java.util.List;

public interface VpaSource {

  VpaSourceType getType();

  List<VpaDescriptor> getDescriptors();

  boolean delete(VpaDescriptor descriptor);

  String getLocation();

  void invalidate();

  File getFile(VpaDescriptor vpaDescriptor);
}
