package de.mephisto.vpin.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;

public class GithubUtil {
  private final static Logger LOG = LoggerFactory.getLogger(GithubUtil.class);

  public static String checkForUpdate(String referenceVersion, String latestReleaseUrl) {
    try {
      URL obj = new URL(latestReleaseUrl);
      HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
      conn.setInstanceFollowRedirects(true);
      HttpURLConnection.setFollowRedirects(true);
      conn.setReadTimeout(5000);
      conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
      conn.addRequestProperty("User-Agent", "Mozilla");
      conn.addRequestProperty("Referer", "google.com");

      String s = conn.getURL().toString();
      String versionSegment = s.substring(s.lastIndexOf("/") + 1);
      if (!referenceVersion.equalsIgnoreCase(versionSegment)) {
        return versionSegment;
      }
    } catch (Exception e) {
      LOG.error("Update check failed: " + e.getMessage());
    }
    return null;
  }
}
