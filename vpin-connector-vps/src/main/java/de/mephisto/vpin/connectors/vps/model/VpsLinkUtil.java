package de.mephisto.vpin.connectors.vps.model;

public class VpsLinkUtil {

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
      return "VPU";
    }

    if (url.contains("dropbox.")) {
      return "Dropbox";
    }


    return "Ext";
  }

}
