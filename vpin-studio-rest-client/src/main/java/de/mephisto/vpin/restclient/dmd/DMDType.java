package de.mephisto.vpin.restclient.dmd;

public enum DMDType {
  NoDMD, VirtualDMD, AlphaNumericDMD, VpinMAMEDMD;

  @Override
  public String toString() {
    switch (this) {
      case NoDMD: {
        return "No DMD";
      }
      case VirtualDMD: {
        return "Virtual DMD";
      }
      //case AlphaNumericDMD: {
      //  return "AlphaNumeric DMD";
      //}
      case VpinMAMEDMD: {
        return "Visual PinMAME DMD";
      }
      default: {
        return null;
      }
    }
  }
}
