package de.mephisto.vpin.server.directb2s;

public enum B2SImageRatio {
  RATIO_16x9,
  RATIO_4x3;

  public int getXRatio() {
    if (this.equals(RATIO_16x9)) {
      return 16;
    }
    return 4;
  }

  public int getYRatio() {
    if (this.equals(RATIO_16x9)) {
      return 9;
    }
    return 3;
  }
}
