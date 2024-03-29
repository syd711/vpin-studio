package de.mephisto.vpin.connectors.vps.model;

import java.util.ArrayList;
import java.util.List;

public class VpsAuthoredUrls implements VPSEntity {
  private List<VpsUrl> urls = new ArrayList<>();

  private List<String> authors = new ArrayList<>();
  private String version;
  private long updatedAt;
  private String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

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

  public boolean isContainedIn(List<? extends VpsAuthoredUrls> urls) {
    for (VpsAuthoredUrls url : urls) {
      List<VpsUrl> comparingUrls = url.getUrls();
      if (comparingUrls.isEmpty() && this.getUrls().isEmpty()) {
        return true;
      }

      if (this.getId() != null && url.getId() != null) {
        if (url.getId().equals(this.getId())) {
          return true;
        }
      }

      for (VpsUrl vpsUrl : this.urls) {
        if (url.getUrls().contains(vpsUrl)) {
          return true;
        }
      }

    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VpsAuthoredUrls)) return false;

    VpsAuthoredUrls that = (VpsAuthoredUrls) o;
    if (!String.valueOf(version).equals(String.valueOf(that.version))) return false;
    if (urls == null && that.urls != null) return false;
    if (urls != null && that.urls == null) return false;
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
