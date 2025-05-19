package de.mephisto.vpin.ui;

/**
 * Contains parameters that can be used to initialize a navigation item.
 */
public class NavigationOptions {
  private Object model;
  private int gameId;

  public NavigationOptions(int gameId) {
    this.gameId = gameId;
  }

  public NavigationOptions(Object model) {
    this.model = model;
  }

  public static NavigationOptions empty() {
    return new NavigationOptions(null);
  }

  public Object getModel() {
    return model;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }
}
