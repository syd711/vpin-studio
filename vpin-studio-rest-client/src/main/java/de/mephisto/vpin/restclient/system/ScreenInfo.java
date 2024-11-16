package de.mephisto.vpin.restclient.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.stage.Screen;

public class ScreenInfo {
  private boolean portraitMode;
  private boolean primary;
  private int width;
  private int height;
  private int originalWidth;
  private int originalHeight;
  private int id;
  private double x;
  private double y;
  private Screen screen;

  @JsonIgnore
  public Screen getScreen() {
    return screen;
  }

  public void setScreen(Screen screen) {
    this.screen = screen;
  }

  public int getOriginalWidth() {
    return originalWidth;
  }

  public void setOriginalWidth(int originalWidth) {
    this.originalWidth = originalWidth;
  }

  public int getOriginalHeight() {
    return originalHeight;
  }

  public void setOriginalHeight(int originalHeight) {
    this.originalHeight = originalHeight;
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public boolean isPortraitMode() {
    return portraitMode;
  }

  public void setPortraitMode(boolean portraitMode) {
    this.portraitMode = portraitMode;
  }

  public boolean isPrimary() {
    return primary;
  }

  public void setPrimary(boolean primary) {
    this.primary = primary;
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
  public String toString() {
    if (primary) {
      return "Monitor " + id + " (primary) [" + getOriginalWidth() + "x" + getOriginalHeight() + "]";
    }
    return "Monitor " + id + " [" + getOriginalWidth() + "x" + getOriginalHeight() + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ScreenInfo)) return false;

    ScreenInfo that = (ScreenInfo) o;

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return id;
  }
}
