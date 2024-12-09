package de.mephisto.vpin.restclient.directb2s;

import org.apache.commons.lang3.StringUtils;

public class DirectB2sScreenRes {

  private int emulatorId;
  private String fileName;

  /** optional associated game */
  private int gameId = -1;

  /** Whether this instance represent the global screenres.txt (true) or a per table one (false) */
  private boolean global;

  private String screenresFilePath;

  //---
  private int playfieldWidth;
  private int playfieldHeight;

  //---
  private int backglassWidth;
  private int backglassHeight;

  /** Define Backglass screen using Display Devicename screen number (\\.\DISPLAY)x or screen coordinates (@x) or screen index (=x) */
  private String backglassDisplay;

  private int backglassX;
  private int backglassY;

  //---
  private int dmdWidth;
  private int dmdHeight;

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

  private String backgroundFilePath;

  private String b2SWindowPunch;

  private boolean turnOnRunAsExe;

  private boolean turnOnBackground;


  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
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

  public double getBackglassMinX() {
    return getBackglassX();
  }
  public double getBackglassMaxX() {
    return getBackglassX() + getBackglassWidth();
  }
  public double getBackglassMinY() {
    return getBackglassY();
  }
  public double getBackglassMaxY() {
    return getBackglassY() + getBackglassHeight();
  }

  public double getDmdMinX() {
    return getDmdX();
  }
  public double getDmdMaxX() {
    return getDmdX() + getDmdWidth();
  }
  public double getDmdMinY() {
    return getDmdY();
  }
  public double getDmdMaxY() {
    return getDmdY() + getDmdHeight();
  }

	public boolean isOnBackglass(double x, double y) {
    return getBackglassMinX()<= x && x <= getBackglassMaxX() && getBackglassMinY() <= y && y <= getBackglassMaxY();
	}

  public boolean isOnDmd(double x, double y) {
    return getDmdMinX()<= x && x <= getDmdMaxX() && getDmdMinY() <= y && y <= getDmdMaxY();
  }

  public boolean isBackglassCentered() {
    return getBackgroundWidth() > 0 && getBackgroundHeight() > 0;
  }

  public boolean hasFrame() {
    return StringUtils.isNotEmpty(backgroundFilePath) && isBackglassCentered();
  }

}
