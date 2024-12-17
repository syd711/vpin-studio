package de.mephisto.vpin.commons;

public class MonitorInfo {

  private int screenX;
  private int screenY;
  private int screenWidth;
  private int screenHeight;

  private boolean primary;

  private String deviceName;


  public int getScreenX() {
    return screenX;
  }

  public void setScreenX(int screenX) {
    this.screenX = screenX;
  }

  public int getScreenY() {
    return screenY;
  }

  public void setScreenY(int screenY) {
    this.screenY = screenY;
  }

  public int getScreenWidth() {
    return screenWidth;
  }

  public void setScreenWidth(int screenWidth) {
    this.screenWidth = screenWidth;
  }

  public int getScreenHeight() {
    return screenHeight;
  }

  public void setScreenHeight(int screenHeight) {
    this.screenHeight = screenHeight;
  }

  public boolean isPrimary() {
    return primary;
  }

  public void setPrimary(boolean primary) {
    this.primary = primary;
  }

  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  
}