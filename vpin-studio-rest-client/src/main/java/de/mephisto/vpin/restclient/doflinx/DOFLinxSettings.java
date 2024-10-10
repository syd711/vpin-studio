package de.mephisto.vpin.restclient.doflinx;

import de.mephisto.vpin.restclient.JsonSettings;

/**
 *
 */
public class DOFLinxSettings extends JsonSettings {
  private boolean autostart = false;
  private String installationFolder;

  public boolean isAutostart() {
    return autostart;
  }

  public void setAutostart(boolean autostart) {
    this.autostart = autostart;
  }

  public String getInstallationFolder() {
    return installationFolder;
  }

  public void setInstallationFolder(String installationFolder) {
    this.installationFolder = installationFolder;
  }
}
