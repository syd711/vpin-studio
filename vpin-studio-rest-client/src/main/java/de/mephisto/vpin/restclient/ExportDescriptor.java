package de.mephisto.vpin.restclient;

public class ExportDescriptor {
  private int gameId;
  private VpaManifest manifest;
  private boolean exportRom;
  private boolean exportPupPack;
  private boolean exportPopperMedia;
  private boolean exportHighscores;

  public boolean isExportHighscores() {
    return exportHighscores;
  }

  public void setExportHighscores(boolean exportHighscores) {
    this.exportHighscores = exportHighscores;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public VpaManifest getManifest() {
    return manifest;
  }

  public void setManifest(VpaManifest manifest) {
    this.manifest = manifest;
  }

  public boolean isExportRom() {
    return exportRom;
  }

  public void setExportRom(boolean exportRom) {
    this.exportRom = exportRom;
  }

  public boolean isExportPupPack() {
    return exportPupPack;
  }

  public void setExportPupPack(boolean exportPupPack) {
    this.exportPupPack = exportPupPack;
  }

  public boolean isExportPopperMedia() {
    return exportPopperMedia;
  }

  public void setExportPopperMedia(boolean exportPopperMedia) {
    this.exportPopperMedia = exportPopperMedia;
  }
}
