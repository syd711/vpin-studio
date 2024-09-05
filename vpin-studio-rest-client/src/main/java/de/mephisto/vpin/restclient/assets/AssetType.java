package de.mephisto.vpin.restclient.assets;

import org.apache.commons.lang3.ArrayUtils;

public enum AssetType {
  AVATAR,
  COMPETITION,
  VPIN_AVATAR,
  TABLE,
  CFG,
  NV,
  DIRECTB2S,
  INI,
  PAL,
  PAC,
  CRZ,
  RES,
  VNI,
  ALT_COLOR,
  ALT_SOUND,
  PUP_PACK,
  DMD_PACK,
  POPPER_MEDIA,
  ROM,
  MUSIC,
  MUSIC_BUNDLE,
  POV,
  VPX,
  ZIP,
  RAR,
  ARCHIVE,
  DEFAULT_BACKGROUND,
  CARD_BACKGROUND;

  static AssetType[] INSTALLABLE_ASSET_TYPES = { 
    ZIP, RAR, RES, INI, POV, DIRECTB2S, VNI, PAL, PAC, CRZ, CFG, NV
  };

  public static AssetType fromExtension(String extension) {
    try {
      return AssetType.valueOf(extension.toUpperCase());
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }
  public static boolean isInstallable(String extension) {
    return isInstallable(fromExtension(extension));
  }
  public static boolean isInstallable(AssetType assetType) {
    return ArrayUtils.contains(INSTALLABLE_ASSET_TYPES, assetType);
  }
}
