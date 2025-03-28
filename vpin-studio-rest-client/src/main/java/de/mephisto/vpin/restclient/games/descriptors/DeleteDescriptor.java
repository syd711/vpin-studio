package de.mephisto.vpin.restclient.games.descriptors;

import java.util.ArrayList;
import java.util.List;

public class DeleteDescriptor {
  private boolean deleteTable;
  private boolean deleteDirectB2s;
  private boolean deleteFromFrontend;
  private boolean deletePupPack;
  private boolean deleteDMDs;
  private boolean deleteHighscores;
  private boolean deleteMusic;
  private boolean deleteAltSound;
  private boolean deleteAltColor;
  private boolean deleteCfg;
  private boolean deleteBAMCfg;
  private boolean deletePov;
  private boolean deleteRes;
  private boolean deleteIni;
  private boolean deleteVbs;
  private boolean deletePinVol;
  private boolean keepAssets;

  public boolean isDeleteBAMCfg() {
    return deleteBAMCfg;
  }

  public void setDeleteBAMCfg(boolean deleteBAMCfg) {
    this.deleteBAMCfg = deleteBAMCfg;
  }

  public boolean isDeletePinVol() {
    return deletePinVol;
  }

  public void setDeletePinVol(boolean deletePinVol) {
    this.deletePinVol = deletePinVol;
  }

  public boolean isKeepAssets() {
    return keepAssets;
  }

  public void setKeepAssets(boolean keepAssets) {
    this.keepAssets = keepAssets;
  }

  public boolean isDeleteCfg() {
    return deleteCfg;
  }

  public void setDeleteCfg(boolean deleteCfg) {
    this.deleteCfg = deleteCfg;
  }

  public boolean isDeletePov() {
    return deletePov;
  }

  public void setDeletePov(boolean deletePov) {
    this.deletePov = deletePov;
  }

  public boolean isDeleteRes() {
    return deleteRes;
  }

  public void setDeleteRes(boolean deleteRes) {
    this.deleteRes = deleteRes;
  }

  public boolean isDeleteIni() {
    return deleteIni;
  }

  public void setDeleteIni(boolean deleteIni) {
    this.deleteIni = deleteIni;
  }

  public boolean isDeleteVbs() {
    return deleteVbs;
  }

  public void setDeleteVbs(boolean deleteVbs) {
    this.deleteVbs = deleteVbs;
  }

  private List<Integer> gameIds = new ArrayList<>();

  public List<Integer> getGameIds() {
    return gameIds;
  }

  public void setGameIds(List<Integer> gameIds) {
    this.gameIds = gameIds;
  }

  public boolean isDeleteAltColor() {
    return deleteAltColor;
  }

  public void setDeleteAltColor(boolean deleteAltColor) {
    this.deleteAltColor = deleteAltColor;
  }

  public boolean isDeleteTable() {
    return deleteTable;
  }

  public void setDeleteTable(boolean deleteTable) {
    this.deleteTable = deleteTable;
  }

  public boolean isDeleteDirectB2s() {
    return deleteDirectB2s;
  }

  public void setDeleteDirectB2s(boolean deleteDirectB2s) {
    this.deleteDirectB2s = deleteDirectB2s;
  }

  public boolean isDeleteFromFrontend() {
    return deleteFromFrontend;
  }

  public void setDeleteFromFrontend(boolean deleteFromFrontend) {
    this.deleteFromFrontend = deleteFromFrontend;
  }

  public boolean isDeletePupPack() {
    return deletePupPack;
  }

  public void setDeletePupPack(boolean deletePupPack) {
    this.deletePupPack = deletePupPack;
  }

  public boolean isDeleteDMDs() {
    return deleteDMDs;
  }

  public void setDeleteDMDs(boolean deleteDMDs) {
    this.deleteDMDs = deleteDMDs;
  }

  public boolean isDeleteHighscores() {
    return deleteHighscores;
  }

  public void setDeleteHighscores(boolean deleteHighscores) {
    this.deleteHighscores = deleteHighscores;
  }

  public boolean isDeleteMusic() {
    return deleteMusic;
  }

  public void setDeleteMusic(boolean deleteMusic) {
    this.deleteMusic = deleteMusic;
  }

  public boolean isDeleteAltSound() {
    return deleteAltSound;
  }

  public void setDeleteAltSound(boolean deleteAltSound) {
    this.deleteAltSound = deleteAltSound;
  }
}
