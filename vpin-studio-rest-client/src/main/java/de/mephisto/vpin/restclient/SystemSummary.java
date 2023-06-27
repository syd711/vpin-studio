package de.mephisto.vpin.restclient;

public class SystemSummary {
  private String pinupSystemDirectory;
  private String visualPinballDirectory;
  private String vpinMameDirectory;

  private ScreenInfo screenInfo;

  public ScreenInfo getScreenInfo() {
    return screenInfo;
  }

  public void setScreenInfo(ScreenInfo screenInfo) {
    this.screenInfo = screenInfo;
  }

  public String getPinupSystemDirectory() {
    return pinupSystemDirectory;
  }

  public void setPinupSystemDirectory(String pinupSystemDirectory) {
    this.pinupSystemDirectory = pinupSystemDirectory;
  }

  public String getVisualPinballDirectory() {
    return visualPinballDirectory;
  }

  public void setVisualPinballDirectory(String visualPinballDirectory) {
    this.visualPinballDirectory = visualPinballDirectory;
  }

  public String getVpinMameDirectory() {
    return vpinMameDirectory;
  }

  public void setVpinMameDirectory(String vpinMameDirectory) {
    this.vpinMameDirectory = vpinMameDirectory;
  }
}
