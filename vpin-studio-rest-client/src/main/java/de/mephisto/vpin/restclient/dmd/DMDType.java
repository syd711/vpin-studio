package de.mephisto.vpin.restclient.dmd;

public enum DMDType {
  NoDMD, VirtualDMD, AlphaNumericDMD, VpinMAMEDMD;

  @Override
  public String toString() {
    switch (this) {
      case NoDMD: {
        return "Use Backglass Scores (No DMD)";
      }
      case VirtualDMD: {
        return "Virtual Ext. DMD";
      }
      case AlphaNumericDMD: {
        return "AlphaNumeric Ext. DMD";
      }
      case VpinMAMEDMD: {
        return "Visual PinMAME DMD";
      }
      default: {
        return null;
      }
    }
  }
}
