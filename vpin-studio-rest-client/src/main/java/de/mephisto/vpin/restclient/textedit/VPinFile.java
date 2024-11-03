package de.mephisto.vpin.restclient.textedit;

public enum VPinFile {
  DmdDeviceIni, VPinballXIni, VPMAliasTxt, VBScript, DOFLinxINI, LOCAL;


  @Override
  public String toString() {
    switch (this) {
      case DmdDeviceIni: {
        return "DmdDevice.ini";
      }
      case VPinballXIni: {
        return "VPinballX.ini";
      }
      case VPMAliasTxt: {
        return "VPMAlias.txt";
      }
      case VBScript: {
        return "VB Script";
      }
      case DOFLinxINI: {
        return "DOFLinx.INI";
      }
      case LOCAL: {
        return "Local File";
      }
      default: {
        throw new UnsupportedOperationException("Invalid VPinFile");
      }
    }
  }
}
