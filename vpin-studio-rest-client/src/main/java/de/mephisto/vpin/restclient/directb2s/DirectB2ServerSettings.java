package de.mephisto.vpin.restclient.directb2s;

/**
 *
 */
public class DirectB2ServerSettings implements DirectB2sConstants {

  private String backglassServerFolder;

  private boolean pluginsOn;
  private boolean showStartupError;
  private boolean disableFuzzyMatching;

  private boolean hideGrill;
  private boolean hideB2SDMD;
  private boolean hideDMD;

  /**
   * 0 => FormToBack = 1
   * 1 => FormToFront = 1
   * 2 (Standard) FormToBack = 0 & FormToFront = 0 
   */
  private int formToPosition = FORM_TO_STANDARD;

  /**
   * 2 = exe
   * 1 = Standard
   */
  private int defaultStartMode = EXE_START_MODE;

  public int getDefaultStartMode() {
    return defaultStartMode;
  }

  public void setDefaultStartMode(int defaultStartMode) {
    this.defaultStartMode = defaultStartMode;
  }

  public boolean isDisableFuzzyMatching() {
    return disableFuzzyMatching;
  }

  public void setDisableFuzzyMatching(boolean disableFuzzyMatching) {
    this.disableFuzzyMatching = disableFuzzyMatching;
  }

  public boolean isPluginsOn() {
    return pluginsOn;
  }

  public void setPluginsOn(boolean pluginsOn) {
    this.pluginsOn = pluginsOn;
  }

  public boolean isShowStartupError() {
    return showStartupError;
  }

  public void setShowStartupError(boolean showStartupError) {
    this.showStartupError = showStartupError;
  }

  public String getBackglassServerFolder() {
    return backglassServerFolder;
  }

  public void setBackglassServerFolder(String backglassServerFolder) {
    this.backglassServerFolder = backglassServerFolder;
  }

  public boolean isHideGrill() {
    return hideGrill;
  }

  public void setHideGrill(boolean hideGrill) {
    this.hideGrill = hideGrill;
  }

  public boolean isHideB2SDMD() {
    return hideB2SDMD;
  }

  public void setHideB2SDMD(boolean hideB2SDMD) {
    this.hideB2SDMD = hideB2SDMD;
  }

  public boolean isHideDMD() {
    return hideDMD;
  }

  public void setHideDMD(boolean hideDMD) {
    this.hideDMD = hideDMD;
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
  
}
