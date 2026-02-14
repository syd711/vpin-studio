package de.mephisto.vpin.restclient.mame;

public class MameOptions {
  public final static String DEFAULT_KEY = "default";
  public final static String GLOBALS_KEY = "globals";

  private String rom;

  private boolean skipPinballStartupTest;
  private boolean useSound;
  private boolean useSamples;
  private boolean compactDisplay;
  private boolean doubleDisplaySize;
  private boolean ignoreRomCrcError;
  private boolean cabinetMode;
  private boolean showDmd;
  private boolean useExternalDmd;
  private boolean colorizeDmd;
  private int soundMode;
  private boolean forceStereo;

  public int getSoundMode() {
    return soundMode;
  }

  public void setSoundMode(int soundMode) {
    this.soundMode = soundMode;
  }

  private boolean existInRegistry;

  public boolean isForceStereo() {
    return forceStereo;
  }

  public void setForceStereo(boolean forceStereo) {
    this.forceStereo = forceStereo;
  }

  public boolean isCompactDisplay() {
    return compactDisplay;
  }

  public void setCompactDisplay(boolean compactDisplay) {
    this.compactDisplay = compactDisplay;
  }

  public boolean isDoubleDisplaySize() {
    return doubleDisplaySize;
  }

  public void setDoubleDisplaySize(boolean doubleDisplaySize) {
    this.doubleDisplaySize = doubleDisplaySize;
  }

  public boolean isExistInRegistry() {
    return existInRegistry;
  }

  public void setExistInRegistry(boolean existInRegistry) {
    this.existInRegistry = existInRegistry;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public boolean isSkipPinballStartupTest() {
    return skipPinballStartupTest;
  }

  public void setSkipPinballStartupTest(boolean skipPinballStartupTest) {
    this.skipPinballStartupTest = skipPinballStartupTest;
  }

  public boolean isUseSound() {
    return useSound;
  }

  public void setUseSound(boolean useSound) {
    this.useSound = useSound;
  }

  public boolean isUseSamples() {
    return useSamples;
  }

  public void setUseSamples(boolean useSamples) {
    this.useSamples = useSamples;
  }

  public boolean isIgnoreRomCrcError() {
    return ignoreRomCrcError;
  }

  public void setIgnoreRomCrcError(boolean ignoreRomCrcError) {
    this.ignoreRomCrcError = ignoreRomCrcError;
  }

  public boolean isCabinetMode() {
    return cabinetMode;
  }

  public void setCabinetMode(boolean cabinetMode) {
    this.cabinetMode = cabinetMode;
  }

  public boolean isShowDmd() {
    return showDmd;
  }

  public void setShowDmd(boolean showDmd) {
    this.showDmd = showDmd;
  }

  public boolean isUseExternalDmd() {
    return useExternalDmd;
  }

  public void setUseExternalDmd(boolean useExternalDmd) {
    this.useExternalDmd = useExternalDmd;
  }

  public boolean isColorizeDmd() {
    return colorizeDmd;
  }

  public void setColorizeDmd(boolean colorizeDmd) {
    this.colorizeDmd = colorizeDmd;
  }
}
