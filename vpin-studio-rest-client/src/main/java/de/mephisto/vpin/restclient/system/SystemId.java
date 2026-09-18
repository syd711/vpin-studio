package de.mephisto.vpin.restclient.system;

public class SystemId {
  private String systemName;
  private String version;
  private OperatingSystem operatingSystem;

  public String getSystemName() {
    return systemName;
  }

  public void setSystemName(String systemName) {
    this.systemName = systemName;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public OperatingSystem getOperatingSystem() {
    return operatingSystem;
  }

  public void setOperatingSystem(OperatingSystem operatingSystem) {
    this.operatingSystem = operatingSystem;
  }

}
