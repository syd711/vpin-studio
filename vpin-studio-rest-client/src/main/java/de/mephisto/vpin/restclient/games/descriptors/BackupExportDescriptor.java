package de.mephisto.vpin.restclient.games.descriptors;

import java.util.ArrayList;
import java.util.List;

public class BackupExportDescriptor {
  private List<Integer> gameIds = new ArrayList<>();
  private long backupSourceId;
  private boolean removeFromPlaylists = false;

  public long getBackupSourceId() {
    return backupSourceId;
  }

  public void setBackupSourceId(long backupSourceId) {
    this.backupSourceId = backupSourceId;
  }

  public List<Integer> getGameIds() {
    return gameIds;
  }

  public void setGameIds(List<Integer> gameIds) {
    this.gameIds = gameIds;
  }

  public boolean isRemoveFromPlaylists() {
    return removeFromPlaylists;
  }

  public void setRemoveFromPlaylists(boolean removeFromPlaylists) {
    this.removeFromPlaylists = removeFromPlaylists;
  }
}
