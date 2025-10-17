package de.mephisto.vpin.restclient.frontend;

import edu.umd.cs.findbugs.annotations.NonNull;

public enum EmulatorType {

  VisualPinball("vpx"), VisualPinball9("vpt"),
  FuturePinball("fpt"),
  ZenFX(null), ZenFX2("pxp"), ZenFX3("pxp"), PinballM(null),
  Zaccaria(null),
  PinballArcade(null),
  MAME(null),
  OTHER(null);

  private String extension;

  private EmulatorType(String extension) {
    this.extension = extension;
  }

  public String getExtension() {
    return extension;
  }

  public boolean isVpxEmulator() {
    return VisualPinball.equals(this) || VisualPinball9.equals(this);
  }

  public boolean isFpEmulator() {
    return FuturePinball.equals(this);
  }

  public boolean isPupGameImportSupported() {
    return isZenEmulator() || isZaccariaEmulator();
  }

  public boolean isZenEmulator() {
    return ZenFX.equals(this) || ZenFX2.equals(this) || ZenFX3.equals(this) || PinballM.equals(this);
  }

  public boolean isZaccariaEmulator() {
    return Zaccaria.equals(this);
  }

  public boolean isMameEmulator() {
    return MAME.equals(this);
  }

  public boolean isOther() {
    return OTHER.equals(this);
  }

  public String shortName() {
    switch (this) {
      case VisualPinball:
        return "VPX";
      case FuturePinball:
        return "FP";
      default:
        return "";
    }
  }

  @NonNull
  public String folderName() {
    switch (this) {
      case ZenFX:
        return "Pinball FX";
      case ZenFX2:
        return "Pinball FX2";
      case ZenFX3:
        return "Pinball FX3";
      case PinballM:
        return "Pinball M";
      case Zaccaria:
        return "Zaccaria Pinball";
      case FuturePinball:
        return "Future Pinball";
      case VisualPinball:
        return "Visual Pinball X";
      case VisualPinball9:
        return "Visual Pinball";
      case PinballArcade:
        return "Pinball Arcade";
      case MAME:
        return "MAME";
      case OTHER:
        return "Other";
      default:
        throw new UnsupportedOperationException("No matching emulator type found for " + this);
    }
  }

  public static EmulatorType fromName(String emuName) {
    if (emuName == null) {
      return null;
    }

    String emu = emuName.replaceAll(" ", "").toLowerCase();
    if (emu.startsWith("visualpinball")) {
      return VisualPinball;
    }
    else if (emu.startsWith("futurepinball")) {
      return FuturePinball;
    }
    else if (emu.startsWith("pinballfx2") || emu.toLowerCase().contains("fx2")) {
      return ZenFX2;
    }
    else if (emu.startsWith("pinballfx3") || emu.toLowerCase().contains("fx3")) {
      return ZenFX3;
    }
    else if (emu.startsWith("zaccaria")) {
      return Zaccaria;
    }
    else if (emu.startsWith("pinballarcade")) {
      return PinballArcade;
    }
    else if (emu.contains("mame")) {
      return MAME;
    }
    else {
      return null;
    }
  }

  public static EmulatorType fromExtension(String extension) {
    if (extension == null) {
      return null;
    }

    for (EmulatorType type : EmulatorType.values()) {
      if (type.getExtension() != null && type.getExtension().equalsIgnoreCase(extension)) {
        return type;
      }
    }
    return null;
  }
}
