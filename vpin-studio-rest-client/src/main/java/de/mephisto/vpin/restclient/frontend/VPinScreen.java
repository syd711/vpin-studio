package de.mephisto.vpin.restclient.frontend;

import de.mephisto.vpin.restclient.validation.GameValidationCode;

import java.util.List;

import org.apache.commons.lang3.StringUtils;


public enum VPinScreen {
  Audio(-1, "audio", GameValidationCode.CODE_NO_AUDIO),
  AudioLaunch(-1, "audiolaunch", GameValidationCode.CODE_NO_AUDIO_LAUNCH),
  Other2(8, "GameSelect", GameValidationCode.CODE_NO_OTHER2),
  GameInfo(9, "GameInfo", GameValidationCode.CODE_NO_INFO),
  GameHelp(10, "GameHelp", GameValidationCode.CODE_NO_HELP),
  Topper(0, "Topper", GameValidationCode.CODE_NO_TOPPER),
  BackGlass(2, "Backglass", GameValidationCode.CODE_NO_BACKGLASS),
  Menu(5, "Menu", GameValidationCode.CODE_NO_APRON),
  DMD(1, "DMD", GameValidationCode.CODE_NO_DMD),
  Loading(-1, "Loading", GameValidationCode.CODE_NO_LOADING),
  Wheel(-1, "Wheel", GameValidationCode.CODE_NO_WHEEL_IMAGE),
  PlayField(3, "PlayField", GameValidationCode.CODE_NO_PLAYFIELD);

  private int code;
  private String segment;
  private final int validationCode;

  VPinScreen(int code, String segment, int validationCode) {
    this.code = code;
    this.segment = segment;
    this.validationCode = validationCode;
  }

  public static VPinScreen valueOfScreen(String name) {
    if (Other2.name().equalsIgnoreCase(name)) {
      return Other2;
    }
    if (GameInfo.name().equalsIgnoreCase(name)) {
      return GameInfo;
    }
    if (GameHelp.name().equalsIgnoreCase(name)) {
      return GameHelp;
    }
    if (BackGlass.name().equalsIgnoreCase(name)) {
      return BackGlass;
    }
    if ("Apron/FullDMD".equalsIgnoreCase(name)) {
      return Menu;
    }
    if ("Menu".equalsIgnoreCase(name)) {
      return Menu;
    }
    if ("Apron".equalsIgnoreCase(name)) {
      return Menu;
    }
    if ("Music".equalsIgnoreCase(name)) {
      return Audio;
    }
    if (Topper.name().equalsIgnoreCase(name)) {
      return Topper;
    }
    if (DMD.name().equalsIgnoreCase(name)) {
      return DMD;
    }
    if (PlayField.name().equalsIgnoreCase(name)) {
      return PlayField;
    }
    return null;
  }

  public static FrontendPlayerDisplay valueOfScreen(List<FrontendPlayerDisplay> displays, VPinScreen screen) {
    for (FrontendPlayerDisplay display : displays) {
      if (display.getScreen() != null && display.getScreen().equals(screen)) {
        return display;
      }
    }
    return null;
  }

  public static VPinScreen valueOfSegment(String segment) {
    for (VPinScreen v: values()) {
      if (StringUtils.equalsIgnoreCase(segment, v.segment)) {
        return v;
      }
    }
    return valueOfScreen(segment);
  }

  public int getValidationCode() {
    return validationCode;
  }

  public String getSegment() {
    return segment;
  }

  public int getCode() {
    return code;
  }
}
