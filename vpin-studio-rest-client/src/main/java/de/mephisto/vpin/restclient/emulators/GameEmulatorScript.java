package de.mephisto.vpin.restclient.emulators;

public class GameEmulatorScript {
  private boolean startScript;
  private String script;
  private boolean enabled;
  private boolean waitForExit;
  private boolean hideWindow;
  private String workingDirectory;
  private String executeable;
  private String parameters;

  public boolean isStartScript() {
    return startScript;
  }

  public void setStartScript(boolean startScript) {
    this.startScript = startScript;
  }

  public String getScript() {
    return script;
  }

  public void setScript(String script) {
    this.script = script;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isWaitForExit() {
    return waitForExit;
  }

  public void setWaitForExit(boolean waitForExit) {
    this.waitForExit = waitForExit;
  }

  public boolean isHideWindow() {
    return hideWindow;
  }

  public void setHideWindow(boolean hideWindow) {
    this.hideWindow = hideWindow;
  }

  public String getWorkingDirectory() {
    return workingDirectory;
  }

  public void setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  public String getExecuteable() {
    return executeable;
  }

  public void setExecuteable(String executeable) {
    this.executeable = executeable;
  }

  public String getParameters() {
    return parameters;
  }

  public void setParameters(String parameters) {
    this.parameters = parameters;
  }
}
