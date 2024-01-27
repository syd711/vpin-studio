package de.mephisto.vpin.connectors.vps.model;

public class VpsUtil {

  public static String abbreviate(String url) {
    if (url.length() == 0) {
      return "";
    }

    if (url.contains("vpuniverse.com")) {
      return "VPU";
    }
    if (url.contains("mega.")) {
      return "Mega";
    }

    if (url.contains("vpforums.")) {
      return "VPF";
    }

    if (url.contains("dropbox.")) {
      return "Dropbox";
    }

    if (url.contains("youtube.")) {
      return "YT";
    }

    return "Ext";
  }

  public static String getFeatureColor(String abb) {
    return "#338033";
  }

  public static String getIconClass(String abb) {
    if (abb.equals("VPU")) {
      return "mdi2o-open-in-new";
    }

    if (abb.equals("Dropbox")) {
      return "mdi2d-dropbox";
    }

    if (abb.equals("VPF")) {
      return "mdi2o-open-in-new";
    }

    if (abb.equals("Mega")) {
      return "mdi2o-open-in-new";
    }

    if (abb.equals("YT")) {
      return "mdi2y-youtube";
    }

    return "mdi2o-open-in-new";
  }

  public static String getColor(String abb) {
    if (abb.equals("VPU")) {
      return "#3182ce";
    }

    if (abb.equals("Dropbox")) {
      return "#3182ce";
    }

    if (abb.equals("VPF")) {
      return "#dd6b20";
    }

    if (abb.equals("Mega")) {
      return "#e53e3e";
    }

    if (abb.equals("YT")) {
      return "#ff4e45";
    }

    return "#718096";
  }
}
