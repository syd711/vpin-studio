package de.mephisto.vpin.server.popper;

public enum Emulators {
  VisualPinballX,
  FuturePinball;

  public static String getEmulatorName(Emulators e) {
    switch (e) {
      case VisualPinballX: {
        return "Visual Pinball X";
      }
      case FuturePinball: {
        return "Future Pinball";
      }
    }
    throw new IllegalArgumentException("Illegal emulator enum '" + e + "'");
  }
}
