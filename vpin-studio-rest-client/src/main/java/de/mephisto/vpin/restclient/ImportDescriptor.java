package de.mephisto.vpin.restclient;

public class ImportDescriptor {
  private int gameId;
  private boolean importRom;
  private boolean importPupPack;
  private boolean importPopperMedia;
  private boolean importHighscores;
  private boolean replaceExisting;
  private VpaManifest manifest;

  public VpaManifest getManifest() {
    return manifest;
  }

  public void setManifest(VpaManifest manifest) {
    this.manifest = manifest;
  }

  public boolean isReplaceExisting() {
    return replaceExisting;
  }

  public void setReplaceExisting(boolean replaceExisting) {
    this.replaceExisting = replaceExisting;
  }

  public boolean isImportHighscores() {
    return importHighscores;
  }

  public void setImportHighscores(boolean importHighscores) {
    this.importHighscores = importHighscores;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
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
