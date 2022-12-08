package de.mephisto.vpin.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Updater {
  private final static Logger LOG = LoggerFactory.getLogger(Updater.class);

  private final static String VERSION = "1.0.11";
  private final static String BASE_URL = "https://github.com/syd711/vpin-studio/releases/download/%s/";
  private final static String VERSION_PROPERTIES = "https://raw.githubusercontent.com/syd711/vpin-studio/main/version.properties";

  public static void update(String versionSegment) throws Exception {
    File out = new File("./vpin-extensions.jar");
    String url = String.format(BASE_URL, versionSegment) + "vpin-extensions.jar";
    download(url, out);
  }

  private static void download(String downloadUrl, File target) throws Exception {
    try {
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

  public static String getCurrentVersion() {
    return VERSION;
  }

  public static String checkForUpdate() throws Exception {
    File target = File.createTempFile("vpin-version", ".properties");
    target.deleteOnExit();
    download(VERSION_PROPERTIES, target);
    PropertiesStore store = PropertiesStore.create(target);
    String latestVersion = store.getString("version");
    LOG.info("Latest version available is " + latestVersion);
    target.delete();
    if (latestVersion.equals(VERSION)) {
      return null;
    }
    return latestVersion;
  }
}
