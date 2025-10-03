package de.mephisto.vpin.restclient.assets;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.ArrayUtils;

public enum AssetType {
  AVATAR,
  COMPETITION,
  VPIN_AVATAR,
  TABLE,
  BAM_CFG,
  CFG,
  DIF,
  NV,
  DIRECTB2S,
  INI,
  PAL,
  PAC,
  CRZ,
  RES,
  VNI,
  VPA,
  VBS,
  ALT_COLOR,
  ALT_SOUND,
  PUP_PACK,
  DMD_PACK,
  FRONTEND_MEDIA,
  ROM,
  MUSIC,
  MUSIC_BUNDLE,
  PINVOL,
  POV,
  VPX,
  FPT,
  ZIP,
  RAR,
  SEVENZIP,
  ARCHIVE,
  DEFAULT_BACKGROUND,
  CARD_ASSET;

  static final AssetType[] INSTALLABLE_ASSET_TYPES = {
      ZIP, RAR, SEVENZIP, RES, DIF, INI, POV, DIRECTB2S, VNI, VPA, VPX, FPT, PAL, PAC, CRZ, CFG, BAM_CFG, NV
  };

  public static AssetType fromExtension(@Nullable EmulatorType emulatorType, String extension) {
    try {
      if (extension.equalsIgnoreCase("7z")) {
        return SEVENZIP;
      }

      //correction to BAM config
      AssetType assetType = AssetType.valueOf(extension.toUpperCase());
      if (emulatorType != null && emulatorType.equals(EmulatorType.FuturePinball) && assetType.equals(AssetType.CFG)) {
        assetType = AssetType.BAM_CFG;
      }
      return assetType;
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }

  public static boolean isInstallable(EmulatorType emulatorType, String extension) {
    return isInstallable(fromExtension(emulatorType, extension));
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
      case BAM_CFG: {
        return "BAM .cfg File";
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
      case DIF: {
        return "Patch File";
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
      case VPA: {
        return "VPin Archive";
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
        return "Frontend Media";
      }
      case MUSIC_BUNDLE: {
        return "Music Bundle";
      }
      case TABLE: {
        return "Table";
      }
      default: {
        return this.name();
      }
    }
  }

  public String[] installableExtension() {
    return new String[]{defaultExtension(), "*.zip", "*.rar", "*.7z"};
  }

  public String defaultExtension() {
    switch (this) {
      case RES: {
        return "*.res";
      }
      case POV: {
        return "*.pov";
      }
      case INI: {
        return "*.ini";
      }
      case DIRECTB2S: {
        return "*.directb2s";
      }
      case NV: {
        return "*.nv";
      }
      case ALT_SOUND: {
        return "*.zip";
      }
      case ALT_COLOR: {
        return "*.zip";
      }
      case CFG: {
        return "*.cfg";
      }
      case BAM_CFG: {
        return "*.cfg";
      }
      case FPT: {
        return "*.fpt";
      }
      case ROM: {
        return "*.zip";
      }
      case VPA: {
        return "*.vpa";
      }
      case VPX: {
        return "*.vpx";
      }
      case DMD_PACK: {
        return "DMD Pack";
      }
      case CRZ: {
        return ".cRZ";
      }
      case PAC: {
        return ".pac";
      }
      case PAL: {
        return ".pal";
      }
      case VNI: {
        return ".vni";
      }
      case MUSIC:
      case MUSIC_BUNDLE: {
        return "*.zip";
      }
      case PUP_PACK: {
        return "*.zip";
      }
      default: {
        return null;
      }
    }
  }
}
