package de.mephisto.vpin.restclient.textedit;

public enum VPinFile {
  DmdDeviceIni, VPinballXIni, VPMAliasTxt, VBScript, DOFLinxINI, LOCAL_GAME_FILE;


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
