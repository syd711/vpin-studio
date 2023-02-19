package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.commons.VpaSourceType;

public class VpaSourceAdapterFactory {

  public static VpaSourceAdapter create(VpaSource source) {
    VpaSourceType vpaSourceType = VpaSourceType.valueOf(source.getType());
    switch (vpaSourceType) {
      case File: {
        return new VpaSourceAdapterFileSystem(source);
      }
      default: {
        throw new UnsupportedOperationException("Invalid source type: " + vpaSourceType);
      }
    }
  }
}
