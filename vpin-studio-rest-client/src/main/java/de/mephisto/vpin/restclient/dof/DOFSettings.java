package de.mephisto.vpin.restclient.dof;

import de.mephisto.vpin.restclient.JsonSettings;

public class DOFSettings extends JsonSettings<DOFSettings> {
  private String apiKey;
  private String installationPath;

  //do not delete
  public DOFSettings() {

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
