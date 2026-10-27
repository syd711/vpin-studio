package de.mephisto.vpin.restclient.discord;

/**
 * Installation and runtime status of the locally installed Discord client.
 */
public class DiscordClientStatus {
  private boolean installed;
  private boolean running;
  private String installationFolder;

  public boolean isInstalled() {
    return installed;
  }

  public void setInstalled(boolean installed) {
    this.installed = installed;
  }

  public boolean isRunning() {
    return running;
  }

  public void setRunning(boolean running) {
    this.running = running;
  }

  public String getInstallationFolder() {
    return installationFolder;
  }

  public void setInstallationFolder(String installationFolder) {
    this.installationFolder = installationFolder;
  }
}
