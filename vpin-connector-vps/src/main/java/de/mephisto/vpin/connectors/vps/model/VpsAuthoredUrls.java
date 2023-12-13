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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VpsAuthoredUrls)) return false;

    VpsAuthoredUrls that = (VpsAuthoredUrls) o;
    if(!String.valueOf(version).equals(String.valueOf(that.version))) return false;
    if(urls == null && that.urls != null) return false;
    if(urls != null && that.urls == null) return false;
    if (updatedAt != that.updatedAt) return false;
    if (urls != null && that.urls != null && !urls.equals(that.urls)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = urls.hashCode();
    result = 31 * result + authors.hashCode();
    result = 31 * result + version.hashCode();
    result = 31 * result + (int) (updatedAt ^ (updatedAt >>> 32));
    return result;
  }
}
