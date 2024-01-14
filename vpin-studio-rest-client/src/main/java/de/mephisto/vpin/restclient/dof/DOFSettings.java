package de.mephisto.vpin.restclient.dof;

import de.mephisto.vpin.restclient.JsonSettings;

public class DOFSettings extends JsonSettings {
  private String apiKey;
  private String installationPath;
  private boolean validDOFFolder;

  //do not delete
  public DOFSettings() {

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
