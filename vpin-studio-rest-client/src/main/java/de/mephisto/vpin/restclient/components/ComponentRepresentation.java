package de.mephisto.vpin.restclient.components;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ComponentRepresentation {
  private ComponentType type;

  private List<GithubReleaseRepresentation> releases = new ArrayList<>();

  private boolean installed;

  private Date lastCheck;

  private Date lastModified;

  private String installedVersion;

  private String latestReleaseVersion;

  private String targetFolder;

  private String url;

  public boolean isInstalled() {
    return installed;
  }

  public void setInstalled(boolean installed) {
    this.installed = installed;
  }

  private List<String> exclusions = new ArrayList<>();

  public List<String> getExclusions() {
    return exclusions;
  }

  public void setExclusions(List<String> exclusions) {
    this.exclusions = exclusions;
  }

  public List<GithubReleaseRepresentation> getReleases() {
    return releases;
  }

  public void setReleases(List<GithubReleaseRepresentation> releases) {
    this.releases = releases;
  }

  public String getLatestReleaseVersion() {
    return latestReleaseVersion;
  }

  public void setLatestReleaseVersion(String latestReleaseVersion) {
    this.latestReleaseVersion = latestReleaseVersion;
  }

  public ComponentType getType() {
    return type;
  }

  public void setType(ComponentType type) {
    this.type = type;
  }

  public String getInstalledVersion() {
    return installedVersion;
  }

  public void setInstalledVersion(String installedVersion) {
    this.installedVersion = installedVersion;
  }

  public Date getLastCheck() {
    return lastCheck;
  }

  public void setLastCheck(Date lastCheck) {
    this.lastCheck = lastCheck;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getTargetFolder() {
    return targetFolder;
  }

  public void setTargetFolder(String targetFolder) {
    this.targetFolder = targetFolder;
  }

  public boolean isVersionDiff() {
    if (installedVersion == null || installedVersion.equals("?")) {
      return false;
    }
    if (latestReleaseVersion == null) {
      return false;
    }

    return !installedVersion.equals(latestReleaseVersion);
  }

  @Override
  public String toString() {
    return type.toString();
  }
}
