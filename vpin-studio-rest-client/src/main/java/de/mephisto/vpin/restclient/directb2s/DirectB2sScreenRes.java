package de.mephisto.vpin.restclient.directb2s;

public class DirectB2sScreenRes {

  private int emulatorId;
  private String fileName;

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
}
