package de.mephisto.vpin.restclient;

public class ImportDescriptor {
  private boolean importRom;
  private boolean importPupPack;
  private boolean importPopperMedia;
  private boolean importHighscores;
  private int playlistId = -1;
  private String vpaFileName;

  public String getVpaFileName() {
    return vpaFileName;
  }

  public void setVpaFileName(String vpaFileName) {
    this.vpaFileName = vpaFileName;
  }

  public int getPlaylistId() {
    return playlistId;
  }

  public void setPlaylistId(int playlistId) {
    this.playlistId = playlistId;
  }

  public boolean isImportHighscores() {
    return importHighscores;
  }

  public void setImportHighscores(boolean importHighscores) {
    this.importHighscores = importHighscores;
  }

  public boolean isImportPupPack() {
    return importPupPack;
  }

  public void setImportPupPack(boolean importPupPack) {
    this.importPupPack = importPupPack;
  }

  public boolean isImportPopperMedia() {
    return importPopperMedia;
  }

  public void setImportPopperMedia(boolean importPopperMedia) {
    this.importPopperMedia = importPopperMedia;
  }

  public boolean isImportRom() {
    return importRom;
  }

  public void setImportRom(boolean importRom) {
    this.importRom = importRom;
  }
}
