package de.mephisto.vpin.restclient.vps;

import org.apache.commons.lang3.StringUtils;

public class VpsInstallLink {

  private static final String VPS_INSTALL_LINK_PREFIX = ".vpslnk";

  private int order;
  private String name;
  private String size;
  private String url;

  public static boolean isLinkFilename(String filename) {
    return filename.endsWith(VPS_INSTALL_LINK_PREFIX);
  }

  public static String getLinkFilename(String filename) {
    return filename + VPS_INSTALL_LINK_PREFIX;
  }

  public static String getOriginalFilename(String filename) {
    return StringUtils.substring(filename, 0, -VPS_INSTALL_LINK_PREFIX.length());
  }

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
