package de.mephisto.vpin.server.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class RequestUtil {
  public static boolean doGet(String url) {
    try {
      HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(500);
      connection.setRequestMethod("GET");
      int responseCode = connection.getResponseCode();
      return (200 <= responseCode && responseCode <= 399);
    } catch (IOException exception) {
      return false;
    }
  }
}
