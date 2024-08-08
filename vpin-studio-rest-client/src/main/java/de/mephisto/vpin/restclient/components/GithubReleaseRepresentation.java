package de.mephisto.vpin.restclient.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GithubReleaseRepresentation {
  private String name;
  private String releasesUrl;
  private String url;
  private String releaseNotes;
  private List<String> artifacts = new ArrayList();
  private String tag;

  public String getReleaseNotes() {
    return releaseNotes;
  }

  public void setReleaseNotes(String releaseNotes) {
    this.releaseNotes = releaseNotes;
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    GithubReleaseRepresentation that = (GithubReleaseRepresentation) o;
    return Objects.equals(name, that.name) && Objects.equals(url, that.url) && Objects.equals(tag, that.tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, url, tag);
  }

  @Override
  public String toString() {
    return name;
  }
}
