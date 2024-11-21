package de.mephisto.vpin.restclient.dmd;

import java.util.Objects;

public class DMDInfo {
  private int gameId;
  private int x;
  private int y;
  private int width;
  private int height;
  private String backgroundUrl;

  public String getBackgroundUrl() {
    return backgroundUrl;
  }

  public void setBackgroundUrl(String backgroundUrl) {
    this.backgroundUrl = backgroundUrl;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    DMDInfo dmdInfo = (DMDInfo) object;
    return gameId == dmdInfo.gameId && x == dmdInfo.x && y == dmdInfo.y && width == dmdInfo.width && height == dmdInfo.height && Objects.equals(backgroundUrl, dmdInfo.backgroundUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gameId, x, y, width, height, backgroundUrl);
  }
}
