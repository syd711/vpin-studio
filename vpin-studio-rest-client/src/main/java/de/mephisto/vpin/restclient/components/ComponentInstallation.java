package de.mephisto.vpin.restclient.components;

public class ComponentInstallation {

  private ComponentType component;
  private String artifactName;
  private String releaseTag;
  private String targetFolder;

  public ComponentType getComponent() {
    return component;
  }

  public void setComponent(ComponentType component) {
    this.component = component;
  }

  public String getArtifactName() {
    return artifactName;
  }

  public void setArtifactName(String artifactName) {
    this.artifactName = artifactName;
  }

  public String getReleaseTag() {
    return releaseTag;
  }

  public void setReleaseTag(String releaseTag) {
    this.releaseTag = releaseTag;
  }

  public String getTargetFolder() {
    return targetFolder;
  }

  public void setTargetFolder(String targetFolder) {
    this.targetFolder = targetFolder;
  }

  @Override
  public String toString() {
    return component + " (" + artifactName + ")";
  }
}
