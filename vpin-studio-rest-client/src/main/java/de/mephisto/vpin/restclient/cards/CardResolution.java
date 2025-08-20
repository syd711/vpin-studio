package de.mephisto.vpin.restclient.cards;


public enum CardResolution {
  qHD, HDReady, HD;


  public static CardResolution valueOfString(String newValue) {
    switch (newValue) {
      case "960 x 540": {
        return qHD;
      }
      case "1280 x 720": {
        return HDReady;
      }
      case "1920 x 1080": {
        return HD;
      }
      default: {
        return HDReady;
      }
    }
  }

  @Override
  public String toString() {
    switch (this) {
      case qHD: {
        return "960 x 540";
      }
      case HDReady: {
        return "1280 x 720";
      }
      case HD: {
        return "1920 x 1080";
      }
      default: {
        return "1280 x 720";
      }
    }
  }

  public int toWidth() {
    switch (this) {
      case qHD: {
        return 960;
      }
      case HDReady: {
        return 1280;
      }
      case HD: {
        return 1920;
      }
      default: {
        return 1280;
      }
    }
  }

  public int toHeight() {
    switch (this) {
      case qHD: {
        return 540;
      }
      case HDReady: {
        return 720;
      }
      case HD: {
        return 1080;
      }
      default: {
        return 720;
      }
    }
  }
}
