package de.mephisto.vpin.restclient.games.descriptors;

import java.util.ArrayList;
import java.util.List;

public class BackupExportDescriptor {
  private List<Integer> gameIds = new ArrayList<>();
  private boolean removeFromPlaylists = false;

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
