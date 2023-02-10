package de.mephisto.vpin.restclient;

import java.util.ArrayList;
import java.util.List;

public class ExportDescriptor {
  private List<Integer> gameIds = new ArrayList<>();
  private VpaManifest manifest;
  private boolean exportRom = true;
  private boolean exportPupPack = true;
  private boolean exportPopperMedia = true;
  private boolean exportHighscores = true;

  public boolean isExportHighscores() {
    return exportHighscores;
  }

  public void setExportHighscores(boolean exportHighscores) {
    this.exportHighscores = exportHighscores;
  }

  public List<Integer> getGameIds() {
    return gameIds;
  }

  public void setGameIds(List<Integer> gameIds) {
    this.gameIds = gameIds;
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
