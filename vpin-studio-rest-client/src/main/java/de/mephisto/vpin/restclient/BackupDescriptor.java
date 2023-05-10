package de.mephisto.vpin.restclient;

import java.util.ArrayList;
import java.util.List;

public class BackupDescriptor {
  private List<Integer> gameIds = new ArrayList<>();
  private long repositoryId = -1;
  private boolean exportRom = true;
  private boolean exportPupPack = true;
  private boolean exportPopperMedia = true;
  private boolean exportHighscores = true;
  private boolean removeFromPlaylists = false;

  public boolean isRemoveFromPlaylists() {
    return removeFromPlaylists;
  }

  public void setRemoveFromPlaylists(boolean removeFromPlaylists) {
    this.removeFromPlaylists = removeFromPlaylists;
  }

  public long getRepositoryId() {
    return repositoryId;
  }

  public void setRepositoryId(long repositoryId) {
    this.repositoryId = repositoryId;
  }

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
