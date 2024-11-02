package de.mephisto.vpin.restclient.recorder;

import java.util.ArrayList;
import java.util.List;

public class RecordingData {
  private int activeGameId;
  private List<Integer> gameIds = new ArrayList<>();

  public List<Integer> getGameIds() {
    return gameIds;
  }

  public void setGameIds(List<Integer> gameIds) {
    this.gameIds = gameIds;
  }

  public int getActiveGameId() {
    return activeGameId;
  }

  public void setActiveGameId(int activeGameId) {
    this.activeGameId = activeGameId;
  }
}
