package de.mephisto.vpin.restclient.directb2s;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

public class DirectB2sScreenRes {

  private int emulatorId;
  private String b2sFileName;

  /** optional associated game */
  private int gameId = -1;

  /** Whether this instance represent the global screenres.txt (true) or a per table one (false) */
  private boolean global;

  /** Wether screenres is version 2 */
  private String version;

  /** The path to the screenres that is represented in that bean */
  private String screenresFilePath;

  //---
  private int playfieldWidth;
  private int playfieldHeight;

  //---
  private int backglassWidth;
  private int backglassHeight;

  /** Define Backglass screen using Display Devicename screen number (\.\DISPLAY)x or screen coordinates (@x) or screen index (=x) */
  private String backglassDisplay;

  // the offset of the backlass display relative to playfield
  private int backglassDisplayX;
  private int backglassDisplayY;

  // the offset relative x,y to the backglass display 
  private int backglassX;
  private int backglassY;

  //---
  private int dmdWidth;
  private int dmdHeight;

  // the offset relative x,y to the backglass top left 
  private int dmdX;
  private int dmdY;

  /** Y-flip, flips the LED display upside down */
  private boolean dmdYFlip;

  //---
  /** Background x/y position - relative to the backglass screen - has to be activated in the settings */
  private int backgroundX;
  private int backgroundY;

  private int backgroundWidth;
  private int backgroundHeight;

  /** The path to the background image, if any */
  private String backgroundFilePath;

  private String b2SWindowPunch;

  /** Optionally turns on runAsExe on B2S that is needed to have background */
  private boolean turnOnRunAsExe;

  /** Optionally turns on background property on B2S that is needed to have background */
  private boolean turnOnBackground;

  //-------------------------------------------
  // Computed properties

  @JsonIgnore
  public double getBackglassMinX() {
    return getBackglassDisplayX() + getBackglassX();
  }
  @JsonIgnore
  public double getBackglassMaxX() {
    return getBackglassMinX() + getBackglassWidth();
  }
  @JsonIgnore
  public double getBackglassMinY() {
    return getBackglassDisplayY() + getBackglassY();
  }
  @JsonIgnore
  public double getBackglassMaxY() {
    return getBackglassMinY() + getBackglassHeight();
  }

  @JsonIgnore
  public double getDmdMinX() {
    return getBackglassMinX() + getDmdX();
  }
  @JsonIgnore
  public double getDmdMaxX() {
    return getDmdMinX() + getDmdWidth();
  }
  @JsonIgnore
  public double getDmdMinY() {
    return getBackglassMinY() + getDmdY();
  }
  @JsonIgnore
  public double getDmdMaxY() {
    return getDmdMinY() + getDmdHeight();
  }

  @JsonIgnore
	public boolean isOnBackglass(double x, double y) {
    return getBackglassMinX()<= x && x <= getBackglassMaxX() && getBackglassMinY() <= y && y <= getBackglassMaxY();
	}

  @JsonIgnore
  public boolean isOnFullDmd(double x, double y) {
    return getDmdMinX() <= x && x <= getDmdMaxX() && getDmdMinY() <= y && y <= getDmdMaxY();
  }

  @JsonIgnore
  public boolean isBackglassCentered() {
    return isVersion2() && getBackgroundWidth() > 0 && getBackgroundHeight() > 0;
  }

  @JsonIgnore
  public boolean hasFrame() {
    return StringUtils.isNotEmpty(backgroundFilePath) && isBackglassCentered();
  }

  @JsonIgnore
  public int getFullBackglassX() {
    return isBackglassCentered() ? getBackgroundX() : getBackglassX();
  }
  @JsonIgnore
  public int getFullBackglassY() {
    return isBackglassCentered() ? getBackgroundY() : getBackglassY();
  }
  @JsonIgnore
  public int getFullBackglassWidth() {
    return isBackglassCentered() ? getBackgroundWidth() : getBackglassWidth();
  }
  @JsonIgnore
  public int getFullBackglassHeight() {
    return isBackglassCentered() ? getBackgroundHeight() : getBackglassHeight();
  }

  //-------------------------------------------
  // Getters / Setters

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  public String getB2SFileName() {
    return b2sFileName;
  }

  public void setB2SFileName(String fileName) {
    this.b2sFileName = fileName;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getScreenresFilePath() {
    return screenresFilePath;
  }

  public void setScreenresFilePath(String screenresFilePath) {
    this.screenresFilePath = screenresFilePath;
  }

  public boolean isGlobal() {
    return global;
  }

  public void setGlobal(boolean global) {
    this.global = global;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  // see https://github.com/vpinball/b2s-backglass/blob/7adc7d10b026863529ac3399b6e7235134cb80d0/b2s_screenresidentifier/b2s_screenresidentifier/module.vb#L93
  public boolean isVersion2() {
    return version.replace(" ", "").startsWith("#V2");
  }

  public int getPlayfieldWidth() {
    return playfieldWidth;
  }

  public void setPlayfieldWidth(int playfieldWidth) {
    this.playfieldWidth = playfieldWidth;
  }

  public int getPlayfieldHeight() {
    return playfieldHeight;
  }

  public void setPlayfieldHeight(int playfieldHeight) {
    this.playfieldHeight = playfieldHeight;
  }

  public int getBackglassWidth() {
    return backglassWidth;
  }

  public void setBackglassWidth(int backglassWidth) {
    this.backglassWidth = backglassWidth;
  }

  public int getBackglassHeight() {
    return backglassHeight;
  }

  public void setBackglassHeight(int backglassHeight) {
    this.backglassHeight = backglassHeight;
  }

  public String getBackglassDisplay() {
    return backglassDisplay;
  }

  public void setBackglassDisplay(String backglassDisplay) {
    this.backglassDisplay = backglassDisplay;
  }

  public int getBackglassDisplayX() {
    return backglassDisplayX;
  }

  public void setBackglassDisplayX(int backglassDisplayX) {
    this.backglassDisplayX = backglassDisplayX;
  }

  public int getBackglassDisplayY() {
    return backglassDisplayY;
  }

  public void setBackglassDisplayY(int backglassDisplayY) {
    this.backglassDisplayY = backglassDisplayY;
  }

  public int getBackglassX() {
    return backglassX;
  }

  public void setBackglassX(int backglassX) {
    this.backglassX = backglassX;
  }

  public int getBackglassY() {
    return backglassY;
  }

  public void setBackglassY(int backglassY) {
    this.backglassY = backglassY;
  }

  public int getDmdWidth() {
    return dmdWidth;
  }

  public void setDmdWidth(int dmdWidth) {
    this.dmdWidth = dmdWidth;
  }

  public int getDmdHeight() {
    return dmdHeight;
  }

  public void setDmdHeight(int dmdHeight) {
    this.dmdHeight = dmdHeight;
  }

  public int getDmdX() {
    return dmdX;
  }

  public void setDmdX(int dmdX) {
    this.dmdX = dmdX;
  }

  public int getDmdY() {
    return dmdY;
  }

  public void setDmdY(int dmdY) {
    this.dmdY = dmdY;
  }

  public boolean getDmdYFlip() {
    return dmdYFlip;
  }

  public void setDmYFlip(boolean dmdYFlip) {
    this.dmdYFlip = dmdYFlip;
  }

  public int getBackgroundX() {
    return backgroundX;
  }

  public void setBackgroundX(int backgroundX) {
    this.backgroundX = backgroundX;
  }

  public int getBackgroundY() {
    return backgroundY;
  }

  public void setBackgroundY(int backgroundY) {
    this.backgroundY = backgroundY;
  }

  public int getBackgroundWidth() {
    return backgroundWidth;
  }

  public void setBackgroundWidth(int backgroundWidth) {
    this.backgroundWidth = backgroundWidth;
  }

  public int getBackgroundHeight() {
    return backgroundHeight;
  }

  public void setBackgroundHeight(int backgroundHeight) {
    this.backgroundHeight = backgroundHeight;
  }

  public String getBackgroundFilePath() {
    return backgroundFilePath;
  }

  public void setBackgroundFilePath(String backgroundFilePath) {
    this.backgroundFilePath = backgroundFilePath;
  }

  public String getB2SWindowPunch() {
    return b2SWindowPunch;
  }

  public void setB2SWindowPunch(String b2sWindowPunch) {
    b2SWindowPunch = b2sWindowPunch;
  }

  public boolean isTurnOnRunAsExe() {
    return turnOnRunAsExe;
  }

  public void setTurnOnRunAsExe(boolean turnOnRunAsExe) {
    this.turnOnRunAsExe = turnOnRunAsExe;
  }

  public boolean isTurnOnBackground() {
    return turnOnBackground;
  }

  public void setTurnOnBackground(boolean turnOnBackground) {
    this.turnOnBackground = turnOnBackground;
  }

  public boolean hasFullDmd() {
    return (dmdWidth > 0) && (dmdHeight > 0); 
  }

  //-----------------------------------

  public double getScreenWidth(VPinScreen onScreen) {
    switch (onScreen) {
      case BackGlass:
        return this.getFullBackglassWidth();
      case Menu:
        return this.getDmdWidth();
      case PlayField:
        return this.getPlayfieldWidth();
      default:
        return -1;
    }
  }

  public double getScreenHeight(VPinScreen onScreen) {
    switch (onScreen) {
      case BackGlass:
        return this.getFullBackglassHeight();
      case Menu:
        return this.getDmdHeight();
      case PlayField:
        return this.getPlayfieldHeight();
      default:
        return -1;
    }
  }
}
