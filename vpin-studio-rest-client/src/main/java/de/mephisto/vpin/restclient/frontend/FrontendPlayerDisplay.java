package de.mephisto.vpin.restclient.frontend;

import java.util.Objects;

public class FrontendPlayerDisplay {
  private int monitor;
  private VPinScreen screen;
  private String name;
  private int x;
  private int y;
  private int width;
  private int height;
  private int rotation;
  /**
   * For pinballX, playfield videos are inverted
   * TODO check how much this is redundant with rotation=270
   */
  private boolean inverted;

  public int getMonitor() {
    return monitor;
  }

  public void setMonitor(int monitor) {
    this.monitor = monitor;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public VPinScreen getScreen() {
    return screen;
  }

  public void setScreen(VPinScreen screen) {
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

	public boolean isInverted() {
		return inverted;
	}

	public void setInverted(boolean inverted) {
		this.inverted = inverted;
	}

  /**
   * Return true if the _x, _y point is within the bounds of that display
   */
  public boolean contains(int _x, int _y) {
    return _x >= getMinX() && _x <= getMaxX() && _y >= getMinY() && _y <= getMaxY();
  }
    
  private int getMinX() {
    return getX();
  }

  private int getMaxX() {
    return getX() + getWidth();
  }

  private int getMinY() {
    return getY();
  }

  private int getMaxY() {
    return getY() + getHeight();
  }
          
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    FrontendPlayerDisplay display = (FrontendPlayerDisplay) o;
    return Objects.equals(name, display.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return name + " [" + x + "/" + y + " - " + width + "x" + height + "]";
  }

}
