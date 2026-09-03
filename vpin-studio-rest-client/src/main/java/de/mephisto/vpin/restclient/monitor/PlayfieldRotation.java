package de.mephisto.vpin.restclient.monitor;

public enum PlayfieldRotation {
  ROTATE_0, ROTATE_90, ROTATE_180, ROTATE_270;

  public int getDegrees() {
    return switch (this) {
      case ROTATE_0 -> 0;
      case ROTATE_90 -> 90;
      case ROTATE_180 -> 180;
      case ROTATE_270 -> 270;
    };
  }

  @Override
  public String toString() {
    return getDegrees() + "°";
  }
}
