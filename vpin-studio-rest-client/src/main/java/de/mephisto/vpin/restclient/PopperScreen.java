package de.mephisto.vpin.restclient;

public enum PopperScreen {
  Audio,
  AudioLaunch,
  Other2,
  GameInfo,
  GameHelp,
  Topper,
  BackGlass,
  DMD,
  Loading,
  Wheel,
  PlayField,
  Menu;

  public static int toId(PopperScreen screen) {
    switch (screen) {
      case Topper: {
        return 0;
      }
      case DMD: {
        return 1;
      }
      case BackGlass: {
        return 2;
      }
      case PlayField: {
        return 3;
      }
      case Menu: {
        return 5;
      }
      default: {
        return -1;
      }
    }
  }
}
