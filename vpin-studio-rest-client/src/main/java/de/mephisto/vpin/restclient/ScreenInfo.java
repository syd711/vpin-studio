package de.mephisto.vpin.restclient;

public class ScreenInfo {
  private boolean portraitMode;
  private boolean primary;
  private int width;
  private int height;
  private int id;

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
    return String.valueOf(id);
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
