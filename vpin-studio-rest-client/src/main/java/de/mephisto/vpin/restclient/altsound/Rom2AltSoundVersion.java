package de.mephisto.vpin.restclient.altsound;

/**
 * The installed and the latest released version of rom2altsound.
 */
public class Rom2AltSoundVersion {
  public static final String PROJECT_URL = "https://github.com/Le-Syl21/rom2altsound";
  public static final String RELEASES_URL = PROJECT_URL + "/releases";

  /**
   * false on operating systems the integration does not support
   */
  private boolean supported;
  private boolean installed;
  /**
   * e.g. "0.2.6", null when not installed
   */
  private String installedVersion;
  /**
   * the tag of the latest GitHub release without the leading "v", null when it could not be read
   */
  private String latestVersion;
  private String latestReleaseUrl;
  private boolean updateAvailable;
  private String error;

  public boolean isSupported() {
    return supported;
  }

  public void setSupported(boolean supported) {
    this.supported = supported;
  }

  public boolean isInstalled() {
    return installed;
  }

  public void setInstalled(boolean installed) {
    this.installed = installed;
  }

  public String getInstalledVersion() {
    return installedVersion;
  }

  public void setInstalledVersion(String installedVersion) {
    this.installedVersion = installedVersion;
  }

  public String getLatestVersion() {
    return latestVersion;
  }

  public void setLatestVersion(String latestVersion) {
    this.latestVersion = latestVersion;
  }

  public String getLatestReleaseUrl() {
    return latestReleaseUrl;
  }

  public void setLatestReleaseUrl(String latestReleaseUrl) {
    this.latestReleaseUrl = latestReleaseUrl;
  }

  public boolean isUpdateAvailable() {
    return updateAvailable;
  }

  public void setUpdateAvailable(boolean updateAvailable) {
    this.updateAvailable = updateAvailable;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
