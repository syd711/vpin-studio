package de.mephisto.vpin.restclient.popper;

public class PopperWindowScreen {
  private PopperScreen screen;
  private int x;
  private int y;
  private int width;
  private int height;
  private int rotation;
  private boolean visible;

  public PopperScreen getScreen() {
    return screen;
  }

  public void setScreen(PopperScreen screen) {
    this.screen = screen;
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

  public int getRotation() {
    return rotation;
  }

  public void setRotation(int rotation) {
    this.rotation = rotation;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }
}
