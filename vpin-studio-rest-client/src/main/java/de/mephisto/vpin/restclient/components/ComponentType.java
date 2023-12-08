package de.mephisto.vpin.restclient.components;

public enum ComponentType {
  //do not change order, ordinal is used in DB
  vpinmame, vpinball, b2sbackglass, freezy, flexdmd;


  @Override
  public String toString() {
    switch (this) {
      case vpinmame: {
        return "VPin MAME";
      }
      case vpinball: {
        return "Visual Pinball";
      }
      case b2sbackglass: {
        return "Backglass Server";
      }
      case freezy: {
        return "Freezy";
      }
      case flexdmd: {
        return "FlexDMD";
      }
      default: {
        throw new UnsupportedOperationException("Invalid component type " + this);
      }
    }
  }
}
