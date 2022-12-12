package de.mephisto.vpin.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class Updater {
  private final static Logger LOG = LoggerFactory.getLogger(Updater.class);

  private final static String BASE_URL = "https://github.com/syd711/vpin-studio/releases/download/%s/";
  private final static String LATEST_RELEASE_URL = "https://github.com/syd711/vpin-studio/releases/latest";

  public static String LATEST_VERSION = null;

  public static File updateServer(String versionSegment) throws Exception {
    File out = new File("./vpin-studio-server.jar");
    String url = String.format(BASE_URL, versionSegment) + "vpin-studio-server.jar";
    download(url, out);
    return out;
  }

  public static void startServer() {
    List<String> commands = Arrays.asList("VPin-Studio-Server.exe");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(new File("./"));
    executor.executeCommandAsync();
    LOG.info("Startup command finished.");
  }

  public static void restartClient() {
    List<String> commands = Arrays.asList("VPin-Studio.exe");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(new File("./"));
    executor.executeCommandAsync();
    System.exit(0);
  }

  public static void updateUI(String versionSegment) throws Exception {
    File out = new File("./vpin-studio-ui.jar");
    String url = String.format(BASE_URL, versionSegment) + "vpin-studio-ui.jar";
    download(url, out);
  }

  private static void download(String downloadUrl, File target) throws Exception {
    try {
      LOG.info("Downloading " + downloadUrl);
      URL url = new URL(downloadUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      BufferedInputStream in = new BufferedInputStream(url.openStream());
      FileOutputStream fileOutputStream = new FileOutputStream(target);
      byte dataBuffer[] = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
        fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
      in.close();
      fileOutputStream.close();
      LOG.info("Downloaded update file " + target.getAbsolutePath());
    } catch (Exception e) {
      LOG.error("Failed to execute update: " + e.getMessage(), e);
      throw e;
    }
  }

  public static String checkForUpdate(String referenceVersion) {
    try {
      URL obj = new URL(LATEST_RELEASE_URL);
      HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
      conn.setInstanceFollowRedirects(true);
      HttpURLConnection.setFollowRedirects(true);
      conn.setReadTimeout(5000);
      conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
      conn.addRequestProperty("User-Agent", "Mozilla");
      conn.addRequestProperty("Referer", "google.com");
      int responseCode = conn.getResponseCode();

      String s = conn.getURL().toString();
      String versionSegment = s.substring(s.lastIndexOf("/") + 1);
      if (!referenceVersion.equalsIgnoreCase(versionSegment)) {
        LATEST_VERSION = versionSegment;
        return versionSegment;
      }
    } catch (Exception e) {
      LOG.error("Update check failed: " + e.getMessage(), e);
    }
    return null;
  }
}
