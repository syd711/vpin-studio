package de.mephisto.vpin.connectors.vps.model;

import java.util.List;

public class VpsAuthoredUrls {
  private List<VpsUrl> urls;

  private List<String> authors;
  private String version;
  private long updatedAt;

  public List<String> getAuthors() {
    return authors;
  }

  public void setAuthors(List<String> authors) {
    this.authors = authors;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public long getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(long updatedAt) {
    this.updatedAt = updatedAt;
  }

  public List<VpsUrl> getUrls() {
    return urls;
  }

  public void setUrls(List<VpsUrl> urls) {
    this.urls = urls;
  }
}
