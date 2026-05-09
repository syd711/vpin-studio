package de.mephisto.vpin.restclient.cards;

import com.fasterxml.jackson.annotation.JsonCreator;

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


  @JsonCreator
  public static CardResolution fromJson(String value) {
    if (value == null) return HDReady;
    for (CardResolution res : values()) {
      if (res.name().equals(value) || res.toString().equals(value)) {
        return res;
      }
    }
    return HDReady;
  }

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
