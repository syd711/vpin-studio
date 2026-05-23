package de.mephisto.vpin.restclient.textedit;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum VPinFile {
  DmdDeviceIni, VPinballXIni, VPMAliasTxt, VBScript, DOFLinxINI, LOCAL_GAME_FILE;

  @JsonCreator
  public static VPinFile fromValue(String value) {
    if (value == null) return null;
    for (VPinFile f : values()) {
      if (f.name().equals(value) || f.toString().equals(value)) {
        return f;
      }
    }
    return null;
  }

  @Override
  public String toString() {
      return switch (this) {
          case DmdDeviceIni -> "DmdDevice.ini";
          case VPinballXIni -> "VPinballX.ini";
          case VPMAliasTxt -> "VPMAlias.txt";
          case VBScript -> "VB Script";
          case DOFLinxINI -> "DOFLinx.INI";
          case LOCAL_GAME_FILE -> "Local File";
          default -> throw new UnsupportedOperationException("Invalid VPinFile");
      };
  }
}
