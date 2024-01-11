package de.mephisto.vpin.restclient.popper;

import org.springframework.lang.Nullable;

public enum PopperScreen {
  Audio(-1, "audio"),
  AudioLaunch(-1, "audiolaunch"),
  Other2(-1, "GameSelect"),
  GameInfo(-1, "GameInfo"),
  GameHelp(-1, "GameHelp"),
  Topper(0, "Topper"),
  BackGlass(2, "Backglass"),
  Menu(5, "Menu"),
  DMD(1, "DMD"),
  Loading(-1, "Loading"),
  Wheel(-1, "Wheel"),
  PlayField(3, "PlayField");

  private int code;
  private String segment;

  PopperScreen(int code, String segment) {
    this.code = code;
    this.segment = segment;
  }

  @Nullable
  public static PopperScreen valueOfScreen(String name) {
    if (Other2.name().equalsIgnoreCase(name)) {
      return Other2;
    }
    if (GameInfo.name().equalsIgnoreCase(name)) {
      return GameInfo;
    }
    if (GameHelp.name().equalsIgnoreCase(name)) {
      return GameHelp;
    }
    return null;
  }

  public String getSegment() {
    return segment;
  }

  public int getCode() {
    return code;
  }
}
