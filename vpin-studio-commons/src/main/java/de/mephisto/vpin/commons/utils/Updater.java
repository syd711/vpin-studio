package de.mephisto.vpin.commons.utils;

import de.mephisto.vpin.commons.utils.scripts.MacOS;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.OSUtil;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Updater {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    File out = new File(getWriteableBaseFolder(), targetZip);
    if (out.exists()) {
      out.delete();
    }
    String url = String.format(BASE_URL, versionSegment) + targetZip;
    download(url, out);
    return true;
  }

  public static int getDownloadProgress(String targetZip, long estimatedSize) {
    File tmp = new File(getWriteableBaseFolder(), targetZip + DOWNLOAD_SUFFIX);
    File zip = new File(getWriteableBaseFolder(), targetZip);
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
      String CheckBasePath = getWriteableBaseFolder().getAbsolutePath();
      LOG.info("Setting tmp File at Base Path : " + CheckBasePath + ":" + target.getName() + ":" + DOWNLOAD_SUFFIX);

      File tmp = new File(getWriteableBaseFolder(), target.getName() + DOWNLOAD_SUFFIX);

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
        LOG.error("Failed to overwrite target file \"{}\"", target.getAbsolutePath());
        return;
      }

      if (!FileUtils.checkedCopy(tmp, target)) {
        LOG.error("Failed to copy  download temp file {} to {}", tmp.getAbsolutePath(), target.getAbsolutePath());
      }
      LOG.info("Downloaded file {}", target.getAbsolutePath());
      if (tmp.delete()) {
        LOG.info("Downloaded temp file {}", tmp.getAbsolutePath());
      }
      else {
        LOG.info("Failed to deleted downloaded temp file {}", tmp.getAbsolutePath());
      }
    }
    catch (Exception e) {
      LOG.error("Updater Failed to execute download: " + e.getMessage(), e);
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
    executor.setDir(getWriteableBaseFolder());
    executor.executeCommandAsync();
    return true;
  }

  public static boolean installClientUpdate(@Nullable String oldVersion, @Nullable String newVersion) throws IOException {
    if (OSUtil.isWindows()) {
      String cmds = "timeout /T 4 /nobreak\ncd /d %~dp0\nresources\\7z.exe -aoa x \"VPin-Studio.zip\"\ntimeout /T 4 /nobreak\ndel VPin-Studio.zip\nVPin-Studio.exe\nexit";
      FileUtils.writeBatch("update-client.bat", cmds);
      LOG.info("Written temporary batch: " + cmds);
      List<String> commands = Arrays.asList("cmd", "/c", "start", "update-client.bat");
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(getWriteableBaseFolder());
      executor.executeCommandAsync();
      new Thread(() -> {
        try {
          Thread.sleep(2000);
          System.exit(0);
        }
        catch (InterruptedException e) {
          //ignore
        }
      }).start();
    }
    else if (OSUtil.isLinux()) {
      try {
        String cmds = "#!/bin/bash\nsleep 4\nunzip -o vpin-studio-ui-jar.zip\nrm vpin-studio-ui-jar.zip\n./VPin-Studio.sh &";
        File file = FileUtils.writeBatch("update-client.sh", cmds);
        LOG.info("Written temporary bash: " + cmds);

        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(file.toPath(), perms);
        LOG.info("Applied execute permissions to : " + file.getAbsolutePath());

        List<String> commands = Arrays.asList("./update-client.sh");
        SystemCommandExecutor executor = new SystemCommandExecutor(commands, false);
        executor.setDir(getWriteableBaseFolder());
        executor.enableLogging(true);
        executor.executeCommandAsync();
        new Thread(() -> {
          try {
            LOG.info("Exiting Studio");
            Thread.sleep(2000);
            System.exit(0);
          }
          catch (InterruptedException e) {
            //ignore
          }
        }).start();
      }
      catch (Exception e) {
        LOG.error("Failed to execute update: " + e.getMessage(), e);
      }
    }
    else if (OSUtil.isMac()) {
      // For the macOS we'll use our startup bash to perform our upgrade.
      try {
        // Create update-client script.
        MacOS.createUpdateScript();

        MacOS.UpdateAppVersion(oldVersion, newVersion);

        // Log the exit message
        LOG.info("Exiting VPin-Studio to perform update...");
        MacOS.launchUpdateScript();
      }
      catch (Exception e) {
        LOG.error("Failed to execute update and restart: {}", e.getMessage(), e);
      }
    }
    return true;
  }

  public static void restartServer() {
    List<String> commands = Arrays.asList("VPin-Studio-Server.exe");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(getWriteableBaseFolder());
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
    if (versionA == null || versionB == null) {
      return false;
    }

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

  public static File getWriteableBaseFolder() {
    if (!OSUtil.isMac()) {
      LOG.info("Setting Base Path for Download to ./");
      return new File("./");
    }
    else {
      LOG.info("Setting Base Path for Mac Download to -" + System.getProperty("MAC_WRITE_PATH"));
      return new File(System.getProperty("MAC_WRITE_PATH"));
    }
  }
}
