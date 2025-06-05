package de.mephisto.vpin.ui.apng.image;

import de.mephisto.vpin.ui.apng.chunks.ApngColorType;

public class ApngFrame {

  int width;
  int height;
  long[] pixels;
  int delayMillis;

  public ApngFrame(int width, int height, int delayMillis) {
    this.width = width;
    this.height = height;
    this.pixels = new long[width * height];
    this.delayMillis = delayMillis;
  }

  public ApngFrame(ApngFrame mainFrame, int delayMillis) {
    this(mainFrame.getWidth(), mainFrame.getHeight(), delayMillis);
    System.arraycopy(mainFrame.getPixels(), 0, pixels, 0, width * height);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getDelayMillis() {
    return delayMillis;
  }

  public long getPixel(int x, int y) {
    return pixels[getIndex(x, y)];
  }

  public long[] getPixels() {
    return pixels;
  }

  public void setPixel(int x, int y, long val) {
    pixels[getIndex(x, y)] = val;
  }

  private int getIndex(int x, int y) {
    if (0 <= x && x < width && 0 <= y && y < height)
      return y * width + x;
    else {
      throw new IndexOutOfBoundsException(String.format(
        "(x,y) = (%d,%d); (width,height) = (%d,%d)", x, y, width, height));
    }
  }

  public byte[] getBytes(ApngColorType colorType) {
    int cpp = colorType.getComponentsPerPixel();
    byte[] rgba = new byte[pixels.length * cpp];
    for (int i = 0; i < pixels.length; i++) {
      int j = 0;
      rgba[cpp * i + j++ ] = (byte) ((pixels[i] >> 16) & 0xff); // R
      if (colorType.equals(ApngColorType.TRUECOLOR) || colorType.equals(ApngColorType.TRUECOLOR_ALPHA)) {
        rgba[cpp * i + j++] = (byte) ((pixels[i] >>  8) & 0xff); // G
        rgba[cpp * i + j++] = (byte) ((pixels[i]      ) & 0xff); // B
      }
      if (colorType.equals(ApngColorType.GREYSCALE_ALPHA) || colorType.equals(ApngColorType.TRUECOLOR_ALPHA)) {
        rgba[cpp * i + j++] = (byte) ((pixels[i] >> 24) & 0xff); // A
      }
    }
    return rgba;
  }
}
