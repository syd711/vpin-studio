package de.mephisto.vpin.restclient.assets;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import org.jspecify.annotations.Nullable;
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
  FPL,
  INI,
  PAL,
  PAC,
  CRZ,
  CROMC,
  RES,
  VNI,
  VPA,
  VBS,
  ALT_COLOR,
  ALT_SOUND,
  PUP_PACK,
  DMD_PACK,
  FP_MODEL_PACK,
  FRONTEND_MEDIA,
  ROM,
  MUSIC,
  MUSIC_BUNDLE,
  PINVOL,
  POV,
  VPX,
  VPT,
  FPT,
  ZIP,
  RAR,
  SEVENZIP,
  ARCHIVE,
  DEFAULT_BACKGROUND,
  CARD_ASSET;

  static final AssetType[] INSTALLABLE_ASSET_TYPES = {
      ZIP, RAR, SEVENZIP, RES, DIF, INI, POV, DIRECTB2S, VNI, VPA, VPX, VPT, FPT, PAL, PAC, CROMC, CRZ, CFG, BAM_CFG, NV, FPL
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
      return switch (this) {
          case NV -> "NV RAM";
          case RES -> ".res File";
          case POV -> ".pov File";
          case INI -> ".ini File";
          case BAM_CFG -> "BAM .cfg File";
          case DIRECTB2S -> "Backglass";
          case ALT_SOUND -> "ALT Sound Bundle";
          case ALT_COLOR -> "ALT Color File";
          case CFG -> ".cfg File";
          case DIF -> "Patch File";
          case FPT -> "FP Table";
          case FPL -> "FP Library";
          case ROM -> "ROM";
          case VPX -> "VPX Table";
          case VPT -> "VP9 Table";
          case VPA -> "VPin Archive";
          case DMD_PACK -> "DMD Pack";
          case FP_MODEL_PACK -> "FP Model Pack";
          case CRZ -> ".cRZ File";
          case CROMC -> ".cROMc File";
          case PAC -> ".pac File";
          case PAL -> ".pal File";
          case VNI -> ".vni File";
          case MUSIC -> "Music File";
          case PUP_PACK -> "PUP Pack";
          case FRONTEND_MEDIA -> "Frontend Media";
          case MUSIC_BUNDLE -> "Music Bundle";
          case TABLE -> "Table";
          default -> this.name();
      };
  }

  public String[] installableExtension() {
    return new String[]{defaultExtension(), "*.zip", "*.rar", "*.7z"};
  }

  public String defaultExtension() {
      return switch (this) {
          case RES -> "*.res";
          case POV -> "*.pov";
          case INI -> "*.ini";
          case DIRECTB2S -> "*.directb2s";
          case NV -> "*.nv";
          case ALT_SOUND -> "*.zip";
          case ALT_COLOR -> "*.zip";
          case CFG -> "*.cfg";
          case BAM_CFG -> "*.cfg";
          case FPT -> "*.fpt";
          case FPL -> "*.fpl";
          case ROM -> "*.zip";
          case VPA -> "*.vpa";
          case VPX -> "*.vpx";
          case VPT -> "*.vpt";
          case DMD_PACK -> "DMD Pack";
          case FP_MODEL_PACK -> "FP Model Pack";
          case CRZ -> ".cRZ";
          case CROMC -> ".cROMc";
          case PAC -> ".pac";
          case PAL -> ".pal";
          case VNI -> ".vni";
          case MUSIC, MUSIC_BUNDLE -> "*.zip";
          case PUP_PACK -> "*.zip";
          default -> null;
      };
  }

  public String getExtension() {
    switch (this) {
      case FP_MODEL_PACK: {
        return "zip";
      }
    }
    return name().toLowerCase();
  }
}
