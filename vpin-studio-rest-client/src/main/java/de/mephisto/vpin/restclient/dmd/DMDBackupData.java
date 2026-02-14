package de.mephisto.vpin.restclient.dmd;

public class DMDBackupData {
  public final static String BACKUP_FILENAME = "dmddevice.json";
  protected int x;
  protected int y;
  protected int width;
  protected int height;

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
}
