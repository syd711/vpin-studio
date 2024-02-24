package de.mephisto.vpin.restclient.dof;

import de.mephisto.vpin.restclient.JsonSettings;

public class DOFSettings extends JsonSettings {
  private String apiKey;
  private String installationPath;
  private String installationPath32;
  private boolean validDOFFolder;
  private boolean validDOFFolder32;

  private boolean syncEnabled;
  private int interval = 7;

  //do not delete
  public DOFSettings() {

  }

  public boolean isSyncEnabled() {
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

  public String getInstallationPath32() {
    return installationPath32;
  }

  public void setInstallationPath32(String installationPath32) {
    this.installationPath32 = installationPath32;
  }

  public boolean isValidDOFFolder32() {
    return validDOFFolder32;
  }

  public void setValidDOFFolder32(boolean validDOFFolder32) {
    this.validDOFFolder32 = validDOFFolder32;
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
