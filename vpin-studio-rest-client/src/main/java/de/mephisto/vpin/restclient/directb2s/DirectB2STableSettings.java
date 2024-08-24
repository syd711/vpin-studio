package de.mephisto.vpin.restclient.directb2s;

public class DirectB2STableSettings {
  private String rom;

  private int hideGrill = 2;
  private boolean hideB2SDMD = false;
  private boolean hideB2SBackglass = false;
  private int hideDMD = 2;
  private int lampsSkipFrames = 1;
  private int solenoidsSkipFrames = 3;
  private int giStringsSkipFrames = 3;
  private int ledsSkipFrames = 0;
  private int usedLEDType = 2;
  private boolean isGlowBulbOn = false;
  private int glowIndex = -1;
  private Boolean startAsEXE = null;
  private boolean startBackground = false;
  private boolean formToFront = false;

  public boolean isHideB2SBackglass() {
    return hideB2SBackglass;
  }

  public void setHideB2SBackglass(boolean hideB2SBackglass) {
    this.hideB2SBackglass = hideB2SBackglass;
  }

  public Boolean getStartAsEXE() {
    return startAsEXE;
  }

  public void setStartAsEXE(Boolean startAsEXE) {
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

  public boolean isStartBackground() {
    return startBackground;
  }

  public void setStartBackground(boolean startBackground) {
    this.startBackground = startBackground;
  }

  public boolean isFormToFront() {
    return formToFront;
  }

  public void setFormToFront(boolean formToFront) {
    this.formToFront = formToFront;
  }
}
