package de.mephisto.vpin.connectors.vps.model;

import java.util.List;

public class VpsAuthoredUrls {
  private List<String> authors;
  private List<VpsUrl> urls;

  public List<String> getAuthors() {
    return authors;
  }

  public void setAuthors(List<String> authors) {
    this.authors = authors;
  }

  public List<VpsUrl> getUrls() {
    return urls;
  }

  public void setUrls(List<VpsUrl> urls) {
    this.urls = urls;
  }
}
