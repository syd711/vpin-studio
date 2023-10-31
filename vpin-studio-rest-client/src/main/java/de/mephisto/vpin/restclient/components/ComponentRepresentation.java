package de.mephisto.vpin.restclient.components;

import java.util.List;

public class ComponentRepresentation {
  private ComponentType type;

  private List<String> artifacts;

  private String installedVersion;

  private String latestReleaseVersion;

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
}
