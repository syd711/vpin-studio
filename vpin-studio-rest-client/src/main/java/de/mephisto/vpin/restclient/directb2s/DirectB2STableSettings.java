package de.mephisto.vpin.restclient.directb2s;

public class DirectB2STableSettings implements DirectB2sConstants {
  public final static String ARCHIVE_FILENAME = "B2STableSettings.json";
  public final static String FILENAME = "B2STableSettings.xml";
  private String rom;

  //private Boolean disableBuiltInEMReelSound = null;
  private int hideGrill = 2;
  private boolean hideB2SDMD = false;
  private boolean hideB2SBackglass = false;
  private int hideDMD = 2;
  private int lampsSkipFrames = 1;
  private int solenoidsSkipFrames = 3;
  private int giStringsSkipFrames = 3;
  private int ledsSkipFrames = 0;
  private int usedLEDType = 0;
  private boolean isGlowBulbOn = false;
  private int glowIndex = -1;
  private int startAsEXE = 2; // standard
  private int startBackground = 2; // standard
  private boolean disableFuzzyMatching = true;
  private int dualMode = 1; //0 NotSet, 1 Authentic, 2 Fantasy

  /**
   * 0 => FormToBack = 1
   * 1 => FormToFront = 1
   * 2 (Standard) FormToBack = 0 & FormToFront = 0 
   */
  private int formToPosition = FORM_TO_STANDARD;

  public boolean isHideB2SBackglass() {
    return hideB2SBackglass;
  }

  public void setHideB2SBackglass(boolean hideB2SBackglass) {
    this.hideB2SBackglass = hideB2SBackglass;
  }

  public int getStartAsEXE() {
    return startAsEXE;
  }

  public void setStartAsEXE(int startAsEXE) {
    this.startAsEXE = startAsEXE;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public int getHideGrill() {
    return hideGrill;
  }

  public void setHideGrill(int hideGrill) {
    this.hideGrill = hideGrill;
  }

  public boolean isHideB2SDMD() {
    return hideB2SDMD;
  }

  public void setHideB2SDMD(boolean hideB2SDMD) {
    this.hideB2SDMD = hideB2SDMD;
  }

  public int getHideDMD() {
    return hideDMD;
  }

  public void setHideDMD(int hideDMD) {
    this.hideDMD = hideDMD;
  }

  public int getLampsSkipFrames() {
    return lampsSkipFrames;
  }

  public void setLampsSkipFrames(int lampsSkipFrames) {
    this.lampsSkipFrames = lampsSkipFrames;
  }

  public int getSolenoidsSkipFrames() {
    return solenoidsSkipFrames;
  }

  public void setSolenoidsSkipFrames(int solenoidsSkipFrames) {
    this.solenoidsSkipFrames = solenoidsSkipFrames;
  }

  public int getGiStringsSkipFrames() {
    return giStringsSkipFrames;
  }

  public void setGiStringsSkipFrames(int giStringsSkipFrames) {
    this.giStringsSkipFrames = giStringsSkipFrames;
  }

  public int getLedsSkipFrames() {
    return ledsSkipFrames;
  }

  public void setLedsSkipFrames(int ledsSkipFrames) {
    this.ledsSkipFrames = ledsSkipFrames;
  }

  public int getUsedLEDType() {
    return usedLEDType;
  }

  public void setUsedLEDType(int usedLEDType) {
    this.usedLEDType = usedLEDType;
  }

  public boolean isGlowBulbOn() {
    return isGlowBulbOn;
  }

  public void setGlowBulbOn(boolean glowBulbOn) {
    isGlowBulbOn = glowBulbOn;
  }

  public int getGlowIndex() {
    return glowIndex;
  }

  public void setGlowIndex(int glowIndex) {
    this.glowIndex = glowIndex;
  }

  public int getStartBackground() {
    return startBackground;
  }

  public void setStartBackground(int startBackground) {
    this.startBackground = startBackground;
  }

  public boolean isDisableFuzzyMatching() {
    return disableFuzzyMatching;
  }

  public void setDisableFuzzyMatching(boolean disableFuzzyMatching) {
    this.disableFuzzyMatching = disableFuzzyMatching;
  }

  public boolean isFormToFront() {
    return getFormToPosition() == FORM_TO_FRONT;
  }
  public boolean isFormToBack() {
    return getFormToPosition() == FORM_TO_BACK;
  }

  public int getFormToPosition() {
    return formToPosition;
  }
  public void setFormToPosition(int formToPosition) {
    this.formToPosition = formToPosition;
  }

  public boolean isHideGrill() {
    return hideGrill == 1;
  }
  public void setHideGrillBoolean(boolean hideGrill) {
    this.hideGrill = hideGrill ? 1 : 0;
  }

  public boolean isHideDMD() {
    return hideDMD == 1;
  }
  public void setHideDMDBoolean(boolean hideDMD) {
    this.hideDMD = hideDMD ? 1 : 0;
  }

  public int getDualMode() {
    return dualMode;
  }
  public void setDualMode(int dualMode) {
    this.dualMode = dualMode;
  }
}
