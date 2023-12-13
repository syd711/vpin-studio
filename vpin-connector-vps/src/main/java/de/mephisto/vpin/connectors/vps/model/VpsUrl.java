package de.mephisto.vpin.connectors.vps.model;

public class VpsUrl {
  private String url;
  private boolean broken;

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public boolean isBroken() {
    return broken;
  }

  public void setBroken(boolean broken) {
    this.broken = broken;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof VpsUrl)) return false;

    VpsUrl vpsUrl = (VpsUrl) o;

    return url.equals(vpsUrl.url);
  }

  @Override
  public int hashCode() {
    return url.hashCode();
  }
}
