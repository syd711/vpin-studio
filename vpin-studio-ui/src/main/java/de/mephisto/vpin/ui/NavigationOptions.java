package de.mephisto.vpin.ui;

/**
 * Contains parameters that can be used to initialize a navigation item.
 */
public class NavigationOptions {
  private int gameId;

  public NavigationOptions(int gameId) {
    this.gameId = gameId;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }
}
