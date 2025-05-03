package de.mephisto.vpin.restclient.directb2s;

import java.util.List;

import de.mephisto.vpin.restclient.validation.ValidationState;

public class DirectB2SDetail {

  private int emulatorId;
  private String filename;
  private int gameId = -1;

  // from Directb2STableSettings
  private int hideGrill;
  private boolean hideB2SDMD;
  private boolean hideBackglass;
  private int hideDMD;

  // from screenres
  private String resPath;
  private String framePath;

  // From DirectB2SData 
  private boolean dmdImageAvailable;

  private boolean isFullDmd;
  
  private int dmdWidth;
  private int dmdHeight;
  private int grillHeight;
  private int nbScores;

  private List<ValidationState> validations;

  //--------------------------------------------------

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
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

  public boolean isHideBackglass() {
    return hideBackglass;
  }

  public void setHideBackglass(boolean hideBackglass) {
    this.hideBackglass = hideBackglass;
  }

  public int getHideDMD() {
    return hideDMD;
  }

  public void setHideDMD(int hideDMD) {
    this.hideDMD = hideDMD;
  }

  public String getResPath() {
    return resPath;
  }

  public void setResPath(String resPath) {
    this.resPath = resPath;
  }

  public String getFramePath() {
    return framePath;
  }

  public void setFramePath(String framePath) {
    this.framePath = framePath;
  }

  public boolean isDmdImageAvailable() {
    return dmdImageAvailable;
  }

  public void setDmdImageAvailable(boolean dmdImageAvailable) {
    this.dmdImageAvailable = dmdImageAvailable;
  }

  public boolean isFullDmd() {
    return isFullDmd;
  }

  public void setFullDmd(boolean isFullDmd) {
    this.isFullDmd = isFullDmd;
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

  public int getGrillHeight() {
    return grillHeight;
  }

  public void setGrillHeight(int grillHeight) {
    this.grillHeight = grillHeight;
  }

  public int getNbScores() {
    return nbScores;
  }

  public void setNbScores(int nbScores) {
    this.nbScores = nbScores;
  }

  public List<ValidationState> getValidations() {
    return validations;
  }

  public void setValidations(List<ValidationState> validations) {
    this.validations = validations;
  }
}
