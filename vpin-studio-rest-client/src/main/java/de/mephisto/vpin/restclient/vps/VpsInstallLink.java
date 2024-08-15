package de.mephisto.vpin.restclient.vps;

public class VpsInstallLink {

  public static final String VPS_INSTALL_LINK_PREFIX = "vpslnk - ";

  private int order;
  private String name;
  private String size;
  private String url;
  
  public VpsInstallLink() {
  }

  public int getOrder() {
    return order;
  }
  public void setOrder(int order) {
    this.order = order;
  }

  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  public String getSize() {
    return size;
  }
  public void setSize(String size) {
    this.size = size;
  }

  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }
}
