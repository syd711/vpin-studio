package de.mephisto.vpin.restclient.components;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ComponentRepresentation {
  private ComponentType type;

  private List<String> artifacts = new ArrayList<>();

  private Date lastCheck;

  private Date lastModified;

  private String installedVersion;

  private String latestReleaseVersion;

  private String url;

  private String targetFolder;

  public List<String> getArtifacts() {
    return artifacts;
  }

  public void setArtifacts(List<String> artifacts) {
    this.artifacts = artifacts;
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
