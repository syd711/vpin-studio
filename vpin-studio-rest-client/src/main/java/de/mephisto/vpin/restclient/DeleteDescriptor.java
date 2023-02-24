package de.mephisto.vpin.restclient;

public class DeleteDescriptor {
  private boolean deleteTable;
  private boolean deleteDirectB2s;
  private boolean deleteFromPopper;
  private boolean deletePupPack;
  private boolean deleteDMDs;
  private boolean deleteHighscores;
  private boolean deleteMusic;
  private boolean deleteAltSound;
  private boolean deleteAltColor;
  private boolean deleteCfg;

  private int gameId;

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public boolean isDeleteAltColor() {
    return deleteAltColor;
  }

  public void setDeleteAltColor(boolean deleteAltColor) {
    this.deleteAltColor = deleteAltColor;
  }

  public boolean isDeleteCfg() {
    return deleteCfg;
  }

  public void setDeleteCfg(boolean deleteCfg) {
    this.deleteCfg = deleteCfg;
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

  public boolean isDeleteFromPopper() {
    return deleteFromPopper;
  }

  public void setDeleteFromPopper(boolean deleteFromPopper) {
    this.deleteFromPopper = deleteFromPopper;
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
