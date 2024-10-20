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
  FRONTEND_MEDIA,
  ROM,
  MUSIC,
  MUSIC_BUNDLE,
  POV,
  VPX,
  FPT,
  ZIP,
  RAR,
  SEVENZIP,
  ARCHIVE,
  DEFAULT_BACKGROUND,
  CARD_BACKGROUND;

  static final AssetType[] INSTALLABLE_ASSET_TYPES = {
    ZIP, RAR, SEVENZIP, RES, INI, POV, DIRECTB2S, VNI, VPX, FPT, PAL, PAC, CRZ, CFG, NV
  };

  public static AssetType fromExtension(String extension) {
    try {
      if(extension.equalsIgnoreCase("7z")) {
        return SEVENZIP;
      }

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


  @Override
  public String toString() {
    switch (this) {
      case NV: {
        return "NV RAM";
      }
      case RES: {
        return ".res File";
      }
      case POV: {
        return ".pov File";
      }
      case INI: {
        return ".ini File";
      }
      case DIRECTB2S: {
        return "Backglass";
      }
      case ALT_SOUND: {
        return "ALT Sound Bundle";
      }
      case ALT_COLOR: {
        return "ALT Color File";
      }
      case CFG: {
        return ".cfg File";
      }
      case FPT: {
        return "FP Table";
      }
      case ROM: {
        return "ROM";
      }
      case VPX: {
        return "VPX Table";
      }
      case DMD_PACK: {
        return "DMD Pack";
      }
      case CRZ: {
        return ".cRZ File";
      }
      case PAC: {
        return ".pac File";
      }
      case PAL: {
        return ".pal File";
      }
      case VNI: {
        return ".vni File";
      }
      case MUSIC: {
        return "Music File";
      }
      case PUP_PACK: {
        return "PUP Pack";
      }
      case FRONTEND_MEDIA: {
        return "Frontend Media File";
      }
      case MUSIC_BUNDLE: {
        return "Music Bundle";
      }
      case TABLE: {
        return "Table File";
      }
    }

    return this.name();
  }
}
