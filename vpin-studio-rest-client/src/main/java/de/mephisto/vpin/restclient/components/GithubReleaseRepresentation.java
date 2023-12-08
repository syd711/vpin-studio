package de.mephisto.vpin.restclient.components;

import java.util.ArrayList;
import java.util.List;

public class GithubReleaseRepresentation {
  private String name;
  private String releasesUrl;
  private String url;
  private List<String> artifacts = new ArrayList();
  private String tag;

  public String getReleasesUrl() {
    return this.releasesUrl;
  }

  public void setReleasesUrl(String releasesUrl) {
    this.releasesUrl = releasesUrl;
  }

  public String getTag() {
    return this.tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return this.url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public List<String> getArtifacts() {
    return this.artifacts;
  }

  public void setArtifacts(List<String> artifacts) {
    this.artifacts = artifacts;
  }
}
