package de.mephisto.vpin.restclient.preferences;

public enum PauseMenuStyle {
  embedded, embeddedAutoStartTutorial, popperScreens;


  @Override
  public String toString() {
    switch (this) {
      case embedded: {
        return "Embed All (Render assets and VPS tutorial links as menu entries)";
      }
      case embeddedAutoStartTutorial: {
        return "Embed Only Assets (Auto-play tutorial video when available)";
      }
      case popperScreens: {
        return "Show Tutorial and Highscore Card Screens (Render remaining assets as menu entries)";
      }
      default: {
        throw new UnsupportedOperationException("Unknown pause menu style");
      }
    }
  }
}
