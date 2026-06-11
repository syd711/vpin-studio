package de.mephisto.vpin.commons.utils;

import de.mephisto.vpin.commons.MacOSUpdater;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.OSUtil;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Updater {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public final static String BASE_URL = "https://github.com/syd711/vpin-studio/releases/download/%s/";
  private final static String LATEST_RELEASE_URL = "https://github.com/syd711/vpin-studio/releases/latest";
  public static String LATEST_VERSION = null;

  public final static String SERVER_ZIP = "VPin-Studio-Server.zip";
  public final static long SERVER_ZIP_SIZE = 306 * 1000 * 1000;

  public final static String UI_ZIP = "VPin-Studio.zip";
  public final static String UI_JAR_ZIP = "vpin-studio-ui-jar.zip";
  public final static long UI_ZIP_SIZE = 157 * 1000 * 1000;

  private final static String JRE_MACOS_X64 = "zulu25.34.17-ca-fx-jre25.0.3-macosx_x64.tar.gz";
  private final static String JRE_MACOS_ARCH64 = "zulu25.34.17-ca-fx-jre25.0.3-macosx_aarch64.tar.gz";
  private final static String JRE_LINUX_X64 = "zulu25.34.17-ca-fx-jre25.0.3-linux_x64.tar.gz";
  private final static String JRE_WIN_X64 = "zulu25.34.17-ca-fx-jre25.0.3-win_x64.zip";

  // must match the IMPLEMENTOR_VERSION prefix in the JRE release file (e.g. "Zulu25.34.17+21-CA")
  private final static String JRE_VERSION_CHECK = "Zulu25.34";

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

    LOG.info("{} download at {}%", tmp.getAbsolutePath(), percentage);
    return percentage;
  }

  public static void download(String downloadUrl, File target) {
    downloadAndOverwrite(downloadUrl, target, false);
  }

  public static void downloadAndOverwrite(String downloadUrl, File target, boolean overwrite) {
    try {
      LOG.info("Downloading {}", downloadUrl);
      URL url = URI.create(downloadUrl).toURL();
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setReadTimeout(5000);
      connection.setUseCaches(false);
      connection.setRequestProperty("Cache-Control", "no-cache, no-store");
      connection.setRequestProperty("Pragma", "no-cache");
      BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
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
        LOG.error("Failed to copy download temp file {} to {}", tmp.getAbsolutePath(), target.getAbsolutePath());
      }
      LOG.info("Download of {}/({}) finished", target.getAbsolutePath(), target.length());
      if (tmp.delete()) {
        LOG.info("Deleted downloaded temp file {}", tmp.getAbsolutePath());
      }
      else {
        LOG.info("Failed to deleted downloaded temp file {}", tmp.getAbsolutePath());
      }
    }
    catch (Exception e) {
      LOG.error("Updater Failed to execute download: {}", e.getMessage(), e);
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
    FileUtils.writeBatch("update-server.bat", loadTemplate("update-server.bat"));
    List<String> commands = Arrays.asList("cmd", "/c", "start", "update-server.bat");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(getWriteableBaseFolder());
    executor.executeCommandAsync();
    return true;
  }

  public static boolean installClientUpdate(@Nullable String oldVersion, @Nullable String newVersion) throws IOException {
    if (OSUtil.isWindows()) {
      String cmds = loadTemplate("update-client-windows.bat");
      FileUtils.writeBatch("update-client.bat", cmds);
      LOG.info("Written temporary batch: {}", cmds);
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
        String cmds = loadTemplate("update-client-linux.sh");
        File file = FileUtils.writeBatch("update-client.sh", cmds);
        LOG.info("Written temporary bash: {}", cmds);

        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(file.toPath(), perms);
        LOG.info("Applied execute permissions to : {}", file.getAbsolutePath());

        List<String> commands = List.of("./update-client.sh");
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
        LOG.error("Failed to execute update: {}", e.getMessage(), e);
      }
    }
    else if (OSUtil.isMac()) {
      // For the macOS we'll use our startup bash to perform our upgrade.
      try {
        // Create update-client script.
        MacOSUpdater.createUpdateScript();

        MacOSUpdater.UpdateAppVersion(oldVersion, newVersion);

        // Log the exit message
        LOG.info("Exiting VPin-Studio to perform update...");
        MacOSUpdater.launchUpdateScript();
      }
      catch (Exception e) {
        LOG.error("Failed to execute update and restart: {}", e.getMessage(), e);
      }
    }
    return true;
  }

  public static void restartServer() {
    List<String> commands = List.of("VPin-Studio-Server.exe");
    SystemCommandExecutor executor = new SystemCommandExecutor(commands);
    executor.setDir(getWriteableBaseFolder());
    executor.executeCommandAsync();
  }

  public static String checkForUpdate() {
    try {
      URL obj = URI.create(LATEST_RELEASE_URL).toURL();
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
      LOG.error("Update check failed: {}", e.getMessage());
    }
    return null;
  }

  public static boolean isLargerVersionThan(String versionA, String versionB) {
    if (versionA == null || versionB == null) {
      return false;
    }

    List<Integer> versionASegments = Arrays.stream(versionA.split("\\.")).map(Integer::parseInt).toList();
    List<Integer> versionBSegments = Arrays.stream(versionB.split("\\.")).map(Integer::parseInt).toList();

    for (int i = 0; i < versionBSegments.size(); i++) {
      if (versionASegments.get(i).intValue() == versionBSegments.get(i).intValue()) {
        continue;
      }

      return versionASegments.get(i) > versionBSegments.get(i);
    }

    return false;
  }

  public static String loadTemplate(String templateName) throws IOException {
    String resourcePath = "/de/mephisto/vpin/commons/utils/" + templateName;
    try (InputStream is = Updater.class.getResourceAsStream(resourcePath)) {
      if (is == null) {
        throw new IOException("Update template not found on classpath: " + resourcePath);
      }
      return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  public static File getWriteableBaseFolder() {
    if (!OSUtil.isMac()) {
      LOG.info("Setting Base Path for Download to ./");
      return new File("./");
    }
    else {
      LOG.info("Setting Base Path for Mac Download to -{}", System.getProperty("MAC_WRITE_PATH"));
      return new File(System.getProperty("MAC_WRITE_PATH"));
    }
  }
}
