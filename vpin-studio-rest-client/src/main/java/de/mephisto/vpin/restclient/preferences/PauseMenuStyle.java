package de.mephisto.vpin.restclient.preferences;

public enum PauseMenuStyle {
  embedded, popperScreens;


  @Override
  public String toString() {
    switch (this) {
      case embedded: {
        return "Embedded (render assets as menu entries)";
      }
      case popperScreens: {
        return "Popper Screens (display assets on Popper screens)";
      }
      default: {
        throw new UnsupportedOperationException("Unknown pause menu style");
      }
    }
  }
}
