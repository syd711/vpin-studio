package de.mephisto.vpin.restclient.popper;

import java.util.Objects;

public class PinUPPlayerDisplay {
  private String name;
  private int x;
  private int y;
  private int width;
  private int height;
  private int rotation;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PinUPPlayerDisplay display = (PinUPPlayerDisplay) o;
    return Objects.equals(name, display.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return name + "[" + x + "/" + y + " - " + width + "x" + height + "]";
  }
}
