package de.mephisto.vpin.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpUtils {
  private final static Logger LOG = LoggerFactory.getLogger(HttpUtils.class);

  /**
   * Checks if a file exists at the given HTTP URL.
   * This method sends a HEAD request to the URL, which typically
   * retrieves only the headers, making it efficient for existence checks.
   * A 200 OK response indicates the file exists.
   *
   * @param fileUrl The full URL of the file to check (e.g., "http://example.com/path/to/file.txt").
   * @return true if the file exists and is accessible (HTTP 200 OK), false otherwise.
   * Returns false if there's a network error, invalid URL, or non-200 response code.
   */
  public static boolean doesFileExist(String fileUrl) {
    HttpURLConnection connection = null;
    try {
      URL url = new URL(fileUrl);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("HEAD");
      connection.connect();
      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        return true;
      }
      else {
        LOG.warn("File does not exist or is not accessible at: " + fileUrl + " (HTTP " + responseCode + ")");
        return false;
      }
    }
    catch (MalformedURLException e) {
      LOG.error("Invalid URL format: " + fileUrl + " - " + e.getMessage());
      return false;
    }
    catch (IOException e) {
      LOG.error("Network or I/O error checking URL: " + fileUrl + " - " + e.getMessage());
      return false;
    }
    finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }
}
