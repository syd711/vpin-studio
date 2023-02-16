package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.commons.VpaSourceType;

import java.util.List;

public interface VpaSource {

  VpaSourceType getType();

  List<VpaDescriptor> getDescriptors();

  boolean delete(VpaDescriptor descriptor);

  String getLocation();
}
