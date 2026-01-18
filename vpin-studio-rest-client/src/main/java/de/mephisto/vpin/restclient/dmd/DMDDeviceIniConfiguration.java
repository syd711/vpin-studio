package de.mephisto.vpin.restclient.dmd;

public class DMDDeviceIniConfiguration {
  private int emulatorId;
  private boolean networkStreamEnabled = false;
  private String webSocketUrl;

  private boolean useRegistry = false;
  private boolean stayOnTop = false;
  private boolean ignoreAspectRatio = false;
  private boolean enabled = true;

  public boolean isStayOnTop() {
    return stayOnTop;
  }

  public void setStayOnTop(boolean stayOnTop) {
    this.stayOnTop = stayOnTop;
  }

  public boolean isIgnoreAspectRatio() {
    return ignoreAspectRatio;
  }

  public void setIgnoreAspectRatio(boolean ignoreAR) {
    this.ignoreAspectRatio = ignoreAR;
  }
  
  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  public boolean isUseRegistry() {
    return useRegistry;
  }

  public void setUseRegistry(boolean useRegistry) {
    this.useRegistry = useRegistry;
  }

  public boolean isNetworkStreamEnabled() {
    return networkStreamEnabled;
  }

  public void setNetworkStreamEnabled(boolean networkStreamEnabled) {
    this.networkStreamEnabled = networkStreamEnabled;
  }

  public String getWebSocketUrl() {
    return webSocketUrl;
  }

  public void setWebSocketUrl(String webSocketUrl) {
    this.webSocketUrl = webSocketUrl;
  }
}
