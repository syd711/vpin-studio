package de.mephisto.vpin.server.vpxz;

import de.mephisto.vpin.restclient.vpxz.VPXZSourceType;

public class VPXZSourceAdapterFactory {

  public static VPXZSourceAdapter create(VPXZService vpxzService, VPXZSource source, VPXZFileService vpxzFileService) {
    VPXZSourceType sourceType = VPXZSourceType.valueOf(source.getType());
      return switch (sourceType) {
          case Folder -> new VPXZSourceAdapterFolder(source);
          case Http -> new VPXZSourceAdapterHttpServer(vpxzService, source);
          default -> throw new UnsupportedOperationException("Invalid source type: " + sourceType);
      };
  }
}
