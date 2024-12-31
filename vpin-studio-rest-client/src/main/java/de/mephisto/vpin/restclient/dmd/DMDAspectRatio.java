package de.mephisto.vpin.restclient.dmd;

public enum DMDAspectRatio {
  ratioOff(1,1), ratio3x1(3,1), ratio4x1(4,1), ratio8x1(8,1);

  int width;
  int height;

  DMDAspectRatio(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }
}
