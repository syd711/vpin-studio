package de.mephisto.vpin.server.directb2s;

public enum DirectB2SImageRatio {
  RATIO_16X9,
  RATIO_4X3;

  public int getXRatio() {
    if (this.equals(RATIO_16X9)) {
      return 16;
    }
    return 4;
  }

  public int getYRatio() {
    if (this.equals(RATIO_16X9)) {
      return 9;
    }
    return 3;
  }
}
