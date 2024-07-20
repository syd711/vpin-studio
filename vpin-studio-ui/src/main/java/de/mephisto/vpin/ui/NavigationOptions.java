package de.mephisto.vpin.ui;

import de.mephisto.vpin.connectors.vps.model.VpsTable;

/**
 * Contains parameters that can be used to initialize a navigation item.
 */
public class NavigationOptions {
  private VpsTable vpsTable;
  private int gameId;

  public NavigationOptions(int gameId) {
    this.gameId = gameId;
  }

  public NavigationOptions(VpsTable vpsTable) {
    this.vpsTable = vpsTable;
  }

  public VpsTable getVpsTable() {
    return vpsTable;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }
}
