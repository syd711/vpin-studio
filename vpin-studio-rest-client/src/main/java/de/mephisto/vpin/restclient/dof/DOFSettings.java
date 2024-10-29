package de.mephisto.vpin.restclient.dof;

import de.mephisto.vpin.restclient.JsonSettings;

public class DOFSettings extends JsonSettings {
  private String apiKey;
  private String installationPath;
  private boolean validDOFFolder;

  private boolean syncEnabled;
  private int interval = 7;

  //do not delete
  public DOFSettings() {

  }

  public boolean getSyncEnabled() {
    return syncEnabled;
  }

  public void setSyncEnabled(boolean syncEnabled) {
    this.syncEnabled = syncEnabled;
  }

  public int getInterval() {
    return interval;
  }

  public void setInterval(int interval) {
    this.interval = interval;
  }

  public boolean isValidDOFFolder() {
    return validDOFFolder;
  }

  public void setValidDOFFolder(boolean validDOFFolder) {
    this.validDOFFolder = validDOFFolder;
  }

  public String getInstallationPath() {
    return installationPath;
  }

  public void setInstallationPath(String installationPath) {
    this.installationPath = installationPath;
  }

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }
}
