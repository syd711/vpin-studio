package de.mephisto.vpin.commons.fx.apng.image;

public class ApngFrame {

  private int width;
  private int height;
  private int bpp;
  private int delayMillis;
  private byte[] bytes;

  public ApngFrame(int width, int height, int bpp, int delayMillis) {
    this.width = width;
    this.height = height;
    this.bpp = bpp;
    this.bytes = new byte[width * height * bpp];
    this.delayMillis = delayMillis;
  }

  public ApngFrame(ApngFrame mainFrame, int delayMillis) {
    this(mainFrame.getWidth(), mainFrame.getHeight(), mainFrame.getBpp(), delayMillis);
    System.arraycopy(mainFrame.getBytes(), 0, bytes, 0, width * height * bpp);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getBpp() {
    return bpp;
  }

  public int getStride() {
    return width * bpp;
  }

  public int getDelayMillis() {
    return delayMillis;
  }

  public byte[] getBytes() {
    return bytes;
  }
}
