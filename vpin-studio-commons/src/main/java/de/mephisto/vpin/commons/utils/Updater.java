package de.mephisto.vpin.commons.utils;

import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.commons.utils.scripts.MacOS;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Updater {
  private final static Logger LOG = LoggerFactory.getLogger(Updater.class);

  public final static String BASE_URL = "https://github.com/syd711/vpin-studio/releases/download/%s/";
  private final static String LATEST_RELEASE_URL = "https://github.com/syd711/vpin-studio/releases/latest";
  public static String LATEST_VERSION = null;

  public final static String SERVER_ZIP = "VPin-Studio-Server.zip";
  public final static String SERVER_EXE = "VPin-Studio-Server.exe";
  public final static long SERVER_ZIP_SIZE = 232 * 1000 * 1000;

  public final static String UI_ZIP = "VPin-Studio.zip";
  public final static String UI_JAR_ZIP = "vpin-studio-ui-jar.zip";
  public final static long UI_ZIP_SIZE = 103 * 1000 * 1000;

  private final static String DOWNLOAD_SUFFIX = ".bak";

  public static boolean downloadUpdate(String versionSegment, String targetZip) {
    File out = new File(getBasePath(), targetZip);
    if (out.exists()) {
      out.delete();
    }
    String url = String.format(BASE_URL, versionSegment) + targetZip;
    download(url, out);
    return true;
  }

  public static int getDownloadProgress(String targetZip, long estimatedSize) {
    File tmp = new File(getBasePath(), targetZip + DOWNLOAD_SUFFIX);
    File zip = new File(getBasePath(), targetZip);
    if (zip.exists()) {
      return 100;
    }

    int percentage = (int) (tmp.length() * 100 / estimatedSize);
    if (percentage > 99) {
      percentage = 99;
    }

    LOG.info(tmp.getAbsolutePath() + " download at " + percentage + "%");
    return percentage;
  }

  public static void download(String downloadUrl, File target) {
    downloadAndOverwrite(downloadUrl, target, false);
  }

  public static void downloadAndOverwrite(String downloadUrl, File target, boolean overwrite) {
    try {
      LOG.info("Downloading " + downloadUrl);
      URL url = new URL(downloadUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setReadTimeout(5000);
      connection.setDoOutput(true);
      BufferedInputStream in = new BufferedInputStream(url.openStream());
      File tmp = new File(getBasePath(), target.getName() + DOWNLOAD_SUFFIX);
      if (tmp.exists()) {
        tmp.delete();
      }
      FileOutputStream fileOutputStream = new FileOutputStream(tmp);
      byte dataBuffer[] = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
        fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
      in.close();
      fileOutputStream.close();
      if (overwrite && target.exists() && !target.delete()) {
        LOG.error("Failed to overwrite target file \"" + target.getAbsolutePath() + "\"");
        return;
      }

      if (!tmp.renameTo(target)) {
        LOG.error("Failed to rename download temp file to " + target.getAbsolutePath());
      }
      LOG.info("Downloaded file " + target.getAbsolutePath());
    } catch (Exception e) {
      LOG.error("Failed to execute download: " + e.getMessage(), e);
    }
  }

  public static void download(String downloadUrl, File target, boolean synchronous) {
    if (synchronous) {
      download(downloadUrl, target);
    }
    else {
      new Thread(() -> {
        download(downloadUrl, target);
      }).start();
    }
  }

  public static boolean installServerUpdate() throws IOException {
    FileUtils.writeBatch("update-server.bat", "timeout /T 8 /nobreak\ncd /d %~dp0\ndel VPin-Studio-Server.exe\nresources\\7z.exe -aoa x \"VPin-Studio-Server.zip\"\ntimeout /T 4 /nobreak\ndel VPin-Studio-Server.zip\nwscript server.vbs\nexit");
    List<String> commands = Arrays.asList("cmd", "/c", "start", "update-server.bat");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(getBasePath());
    executor.executeCommandAsync();
    return true;
  }

  public static boolean installClientUpdate() throws IOException {
    String os = System.getProperty("os.name");
    if (os.contains("Windows")) {
      String cmds = "timeout /T 4 /nobreak\ncd /d %~dp0\nresources\\7z.exe -aoa x \"VPin-Studio.zip\"\ntimeout /T 4 /nobreak\ndel VPin-Studio.zip\nVPin-Studio.exe\nexit";
      FileUtils.writeBatch("update-client.bat", cmds);
      LOG.info("Written temporary batch: " + cmds);
      List<String> commands = Arrays.asList("cmd", "/c", "start", "update-client.bat");
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(getBasePath());
      executor.executeCommandAsync();
      new Thread(() -> {
        try {
          Thread.sleep(2000);
          System.exit(0);
        } catch (InterruptedException e) {
          //ignore
        }
      }).start();
    }
    else {
      // For the macOS we'll use our startup bash to perform our upgrade.
      try {
        // Create update-client script.
        MacOS.createUpdateScript();

        // Create new exec script.
        MacOS.createExecScript();

        // Log the exit message
        LOG.info("Exiting VPin-Studio to perform update...");

        MacOS.launchUpdateScript();

        // Exit the current application
        System.exit(0);
      } catch (Exception e) {
        LOG.error("Failed to execute update and restart: {}", e.getMessage(), e);
      }
    }
    return true;
  }

  public static void restartServer() {
    List<String> commands = Arrays.asList("VPin-Studio-Server.exe");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(getBasePath());
    executor.executeCommandAsync();
  }

  public static String checkForUpdate() {
    try {
      URL obj = new URL(LATEST_RELEASE_URL);
      HttpURLConnection conn = (HttpURLConnection) obj.openConnection();
      conn.setInstanceFollowRedirects(true);
      HttpURLConnection.setFollowRedirects(true);
      conn.setReadTimeout(5000);
      conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
      conn.addRequestProperty("User-Agent", "Mozilla");
      conn.addRequestProperty("Referer", "google.com");

      int responseCode = conn.getResponseCode(); //DO NOT DELETE!!!!

      String s = conn.getURL().toString();
      LATEST_VERSION = s.substring(s.lastIndexOf("/") + 1);
      return LATEST_VERSION;
    }
    catch (Exception e) {
      LOG.error("Update check failed: " + e.getMessage(), e);
    }
    return null;
  }

  public static boolean isLargerVersionThan(String versionA, String versionB) {
    List<Integer> versionASegments = Arrays.asList(versionA.split("\\.")).stream().map(Integer::parseInt).collect(Collectors.toList());
    List<Integer> versionBSegments = Arrays.asList(versionB.split("\\.")).stream().map(Integer::parseInt).collect(Collectors.toList());

    for (int i = 0; i < versionBSegments.size(); i++) {
      if (versionASegments.get(i).intValue() == versionBSegments.get(i).intValue()) {
        continue;
      }

      return versionASegments.get(i) > versionBSegments.get(i);
    }

    return false;
  }

  private static File getBasePath() {
    return new File("./");
  }
}
