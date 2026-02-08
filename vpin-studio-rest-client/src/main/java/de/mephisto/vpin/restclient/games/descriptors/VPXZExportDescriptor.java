package de.mephisto.vpin.restclient.games.descriptors;

import java.util.ArrayList;
import java.util.List;

public class VPXZExportDescriptor {
  private List<Integer> gameIds = new ArrayList<>();
  private long sourceId;

  public long getSourceId() {
    return sourceId;
  }

  public void setSourceId(long sourceId) {
    this.sourceId = sourceId;
  }

  public List<Integer> getGameIds() {
    return gameIds;
  }

  public void setGameIds(List<Integer> gameIds) {
    this.gameIds = gameIds;
  }
}
