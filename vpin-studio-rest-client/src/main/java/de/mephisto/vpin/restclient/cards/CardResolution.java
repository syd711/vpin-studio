package de.mephisto.vpin.restclient.cards;

public enum CardResolution {
  qHD(960, 540), 
  HDReady(1280, 720), 
  HD(1920, 1080), 
  WHEEL(1000, 1000);


  private CardResolution(int width, int height) {
    this.width = width;
    this.height = height;
  }

  private int width;
  private int height;


  public static CardResolution valueOfString(String newValue) {
    for (CardResolution res : CardResolution.values()) {
      if (newValue.equals(res.toString())) {
        return res;
      }
    }
    // not found
    return HDReady;
  }

  @Override
  public String toString() {
    return width + " x " + height;
  }

  public int toWidth() {
    return width;
  }

  public int toHeight() {
    return height;
  }
}
