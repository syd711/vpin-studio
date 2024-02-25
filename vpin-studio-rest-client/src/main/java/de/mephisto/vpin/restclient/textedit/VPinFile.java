package de.mephisto.vpin.restclient.textedit;

public enum VPinFile {
  DmdDeviceIni;


  @Override
  public String toString() {
    switch (this) {
      case DmdDeviceIni: {
        return "DmdDevice.ini";
      }
      default: {
        throw new UnsupportedOperationException("Invalid VPinFile");
      }
    }
  }
}
