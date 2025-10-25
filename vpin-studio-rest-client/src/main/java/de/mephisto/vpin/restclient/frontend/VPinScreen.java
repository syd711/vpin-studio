package de.mephisto.vpin.restclient.frontend;

import de.mephisto.vpin.restclient.validation.GameValidationCode;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;


public enum VPinScreen {
  Audio(4, "audio", GameValidationCode.CODE_NO_AUDIO),
  AudioLaunch(-1, "audiolaunch", GameValidationCode.CODE_NO_AUDIO_LAUNCH),
  Other2(8, "GameSelect", GameValidationCode.CODE_NO_OTHER2),
  GameInfo(9, "GameInfo", GameValidationCode.CODE_NO_INFO),
  GameHelp(10, "GameHelp", GameValidationCode.CODE_NO_HELP),
  Topper(0, "Topper", GameValidationCode.CODE_NO_TOPPER),
  BackGlass(2, "Backglass", GameValidationCode.CODE_NO_BACKGLASS),
  Menu(5, "Menu", GameValidationCode.CODE_NO_APRON),
  DMD(1, "DMD", GameValidationCode.CODE_NO_DMD),
  Loading(7, "Loading", GameValidationCode.CODE_NO_LOADING),
  Wheel(6, "Wheel", GameValidationCode.CODE_NO_WHEEL_IMAGE),
  PlayField(3, "PlayField", GameValidationCode.CODE_NO_PLAYFIELD),
  Logo(99, "Logo", GameValidationCode.CODE_NO_LOGO); // same segment as Wheel for assets search

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
    if ("Apron".equalsIgnoreCase(name)) {
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
    if (Menu.name().equalsIgnoreCase(name)) {
      return Menu;
    }
    if ("Apron/FullDMD".equalsIgnoreCase(name)) {
      return Menu;
    }
    if ("FullDMD".equalsIgnoreCase(name)) {
      return Menu;
    }
    if (Audio.name().equalsIgnoreCase(name)) {
      return Audio;
    }
    if ("Music".equalsIgnoreCase(name)) {
      return Audio;
    }
    if (AudioLaunch.name().equalsIgnoreCase(name)) {
      return AudioLaunch;
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
    if (Loading.name().equalsIgnoreCase(name)) {
      return Loading;
    }
    if (Wheel.name().equalsIgnoreCase(name)) {
      return Wheel;
    }
    if (Logo.name().equalsIgnoreCase(name)) {
      return Logo;
    }
    return null;
  }

  public static VPinScreen valueOfSegment(String segment) {
    for (VPinScreen v : values()) {
      if (StringUtils.equalsIgnoreCase(segment, v.segment)) {
        return v;
      }
    }

    if (segment.equalsIgnoreCase("Other2")) {
      return VPinScreen.Other2;
    }

    return null;
  }

  public static VPinScreen valueOfCode(String code) {
    for (VPinScreen v : values()) {
      if (StringUtils.equalsIgnoreCase(code, "" + v.code)) {
        return v;
      }
    }
    return null;
  }

  public static VPinScreen[] keepDisplaysToScreens(String codes) {
    if (codes == null) {
      return new VPinScreen[0];
    }
    // else
    String[] split = StringUtils.split(codes, ",");
    VPinScreen[] screens = new VPinScreen[split.length];
    for (int i = 0; i < split.length; i++) {
      screens[i] = valueOfCode(split[i].trim());
    }
    return screens;
  }

  public static boolean keepDisplaysContainsScreen(String codes, VPinScreen screen) {
    String[] codesArray = codes != null ? StringUtils.split(codes, ",") : new String[0];
    return ArrayUtils.contains(codesArray, Integer.toString(screen.getCode()));
  }

  public static String keepDisplaysAddScreen(String codes, VPinScreen screen) {
    String[] codesArray = codes != null ? StringUtils.split(codes, ",") : new String[0];
    if (!ArrayUtils.contains(codesArray, Integer.toString(screen.getCode()))) {
      return codes != null ? codes + "," + screen.getCode() : Integer.toString(screen.getCode());
    }
    return codes;
  }

  public static String toString(VPinScreen[] screens) {
    StringBuilder sb = new StringBuilder();
    for (VPinScreen screen : screens) {
      if (sb.length() > 0) {
        sb.append(", ");
      }
      sb.append(screen.name());
    }
    return sb.toString();
  }

  //------------------------------------------

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
