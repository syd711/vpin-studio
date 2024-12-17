package de.mephisto.vpin.restclient.pinvol;

import java.util.ArrayList;
import java.util.List;

public class PinVolUpdate {
  private PinVolTableEntry systemVolume;
  private PinVolTableEntry tableVolume;
  private List<Integer> gameIds = new ArrayList<>();

  public PinVolTableEntry getSystemVolume() {
    return systemVolume;
  }

  public void setSystemVolume(PinVolTableEntry systemVolume) {
    this.systemVolume = systemVolume;
  }

  public PinVolTableEntry getTableVolume() {
    return tableVolume;
  }

  public void setTableVolume(PinVolTableEntry tableVolume) {
    this.tableVolume = tableVolume;
  }

  public List<Integer> getGameIds() {
    return gameIds;
  }

  public void setGameIds(List<Integer> gameIds) {
    this.gameIds = gameIds;
  }
}
