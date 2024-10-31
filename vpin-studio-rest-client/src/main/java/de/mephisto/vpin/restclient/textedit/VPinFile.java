package de.mephisto.vpin.restclient.textedit;

public enum VPinFile {
  DmdDeviceIni, VPMAliasTxt, VBScript, DOFLinxINI;


  @Override
  public String toString() {
    switch (this) {
      case DmdDeviceIni: {
        return "DmdDevice.ini";
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
      default: {
        throw new UnsupportedOperationException("Invalid VPinFile");
      }
    }
  }
}
