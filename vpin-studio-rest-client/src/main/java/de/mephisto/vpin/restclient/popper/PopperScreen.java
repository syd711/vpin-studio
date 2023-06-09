package de.mephisto.vpin.restclient.popper;

public enum PopperScreen {
  Audio,
  AudioLaunch,
  Other2,
  GameInfo,
  GameHelp,
  Topper,
  BackGlass,
  FullDMD,
  DMD,
  Loading,
  Wheel,
  PlayField;

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
      case FullDMD: {
        return 5;
      }
      default: {
        return -1;
      }
    }
  }
}
