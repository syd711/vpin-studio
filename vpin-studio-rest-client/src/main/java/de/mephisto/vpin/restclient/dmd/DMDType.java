package de.mephisto.vpin.restclient.dmd;

public enum DMDType {
  NoDMD, VirtualDMD, AlphaNumericDMD, VpinMAMEDMD;

  @Override
  public String toString() {
      return switch (this) {
          case NoDMD -> "Use Backglass Scores";
          case VirtualDMD -> "Virtual Ext. DMD";
          case AlphaNumericDMD -> "AlphaNumeric Ext. DMD";
          case VpinMAMEDMD -> "Visual PinMAME DMD";
          default -> null;
      };
  }
}
