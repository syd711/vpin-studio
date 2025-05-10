package de.mephisto.vpin.restclient.dmd;

public enum DMDAspectRatio {
  ratioOff(0, 0), ratio3x1(3, 1), ratio4x1(4, 1), ratio8x1(8, 1);

  int width;
  int height;

  DMDAspectRatio(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public Double getValue() {
    return isKeepRatio() ? ((double) width) / height : null; 
  }

  public boolean isKeepRatio() {
    return width > 0 && height > 0;
  }
}
