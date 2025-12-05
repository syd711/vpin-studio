package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.system.FileInfo;
import de.mephisto.vpin.ui.Studio;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

import static de.mephisto.vpin.restclient.util.OSUtil.isMac;
import static de.mephisto.vpin.restclient.util.OSUtil.isWindows;
import static de.mephisto.vpin.ui.Studio.client;

public class SystemUtil {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static String publicUrl = null;

  static {
    UISettings jsonPreference = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    publicUrl = jsonPreference.getWinNetworkShare();
  }

  public static long getMemorySize(String value) {
    long size = 8 * (int) ((((value.length()) * 2) + 45) / 8);
    return size;
  }

  public static boolean isFolderActionSupported() {
    return isLocal() || (!StringUtils.isEmpty(publicUrl) && (isWindows() || isMac()));
  }

  public static void open(FileInfo fileInfo) {
    if (fileInfo != null) {
      if (fileInfo.isFile()) {
        if (fileInfo.getFile() != null && fileInfo.getFile().exists()) {
          openFile(fileInfo.getFile());
        }
        else {
          openFolder(fileInfo.getFallback());
        }
      }
      else {
        openFolder(fileInfo.getFile(), fileInfo.getFallback());
      }
    }
  }


  public static void openFolder(File folder) {
    openFolder(folder, null);
  }

  public static void openFile(File file) {
    if (file == null) {
      return;
    }

    File folder = file.getParentFile();
    if (isLocal()) {
      try {
        if (file.exists()) {
          openFileWithOS(file.getAbsolutePath());
        }
        else if (folder != null && folder.exists()) {
          openFolder(folder);
        }
      }
      catch (IOException e) {
        LOG.error("Failed to open file: " + e.getMessage(), e);
      }
    }
    else {
      openFolder(folder);
    }
  }

  public static void openFolder(File folder, File fallback) {
    if (folder == null) {
      return;
    }

    while (!folder.exists()) {
      folder = folder.getParentFile();
    }

    if (!folder.exists() && (fallback != null && !fallback.exists())) {
      WidgetFactory.showAlert(Studio.stage, "Error", "The local folder \"" + folder.getAbsolutePath() + "\" does not exist.");
      return;
    }

    if (isLocal()) {
      try {
        if (folder.exists()) {
          openFolderWithOS(folder.getAbsolutePath());
        }
        else if (fallback != null && fallback.exists()) {
          openFolderWithOS(fallback.getAbsolutePath());
        }
        else {
          WidgetFactory.showAlert(Studio.stage, "Error", "The local folder \"" + folder.getAbsolutePath() + "\" does not exist.");
        }
      }
      catch (IOException e) {
        LOG.error("Failed to open folder: " + e.getMessage(), e);
      }
    }
    else {
      if (isWindows() || isMac()) {
        try {
          String path = folder.getAbsolutePath();

          String remotePath = resolveNetworkPath(publicUrl, path);
          if (remotePath != null) {
            openFolderWithOS(remotePath);
          }
        }
        catch (Exception e) {
          LOG.error("Failed to open network folder: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open network folder: " + e.getMessage());
        }
      }
    }
  }

  /**
   * Opens the folder specified by the absolute path using the operating system's
   * file explorer.
   *
   * @param absolutePath The absolute path of the folder to open typically from getAbsolutePath().
   * @throws IOException                   If an I/O error occurs.
   * @throws UnsupportedOperationException If the operating system is not supported.
   */
  private static void openFolderWithOS(String absolutePath) throws IOException {
    if (isWindows()) {
      new ProcessBuilder("explorer.exe", absolutePath).start();
    }
    else if (isMac()) {
      new ProcessBuilder("open", absolutePath).start();  // macOS command
    }
    else {
      throw new UnsupportedOperationException("Unsupported operating system: " + System.getProperty("os.name"));
    }
  }

  /**
   * Opens the file specified by the absolute path using the operating system's
   * file explorer, selecting the file if possible.
   *
   * @param absolutePath The absolute path of the file to open typically from getAbsolutePath().
   * @throws IOException                   If an I/O error occurs.
   * @throws UnsupportedOperationException If the operating system is not supported.
   */
  private static void openFileWithOS(String absolutePath) throws IOException {
    if (isWindows()) {
      new ProcessBuilder("explorer.exe", "/select,", absolutePath).start();
    }
    else if (isMac()) {
      new ProcessBuilder("open", "-R", absolutePath).start();
    }
    else {
      throw new UnsupportedOperationException("Unsupported operating system: " + System.getProperty("os.name"));
    }
  }

  /**
   * Resolves a network path based on the provided base path and target path.
   * Supports Windows UNC paths and macOS SMB paths.
   *
   * @param base The base network path.
   * @param path The target path to resolve relative to the base.
   * @return The resolved network path or the original path if base is null.
   */
  public static String resolveNetworkPath(String base, String path) {
    try {
      // If base is null, return the original path
      if (base == null) {
        return path;
      }

      // Handle both Windows UNC and macOS SMB paths
      if (isWindows() && base.startsWith("\\\\")) {
        //TODO cheap workaround to fix issue
        return resolveWindowsNetworkPath(base, path);
      }
      else if (isMac() && base.startsWith("smb://")) {
        // Convert Windows backslashes to forward slashes for SMB paths
        path = path.replace("\\", "/");
        return resolveNetworkPath(base, path, "/", "/");
      }

      // Return null if no matching OS condition was met
      return null;
    }
    catch (Exception e) {
      LOG.error("Failed to resolve network path: " + e.getMessage(), e);
    }
    return path;
  }

  /**
   * Resolves a network path by combining the base and path using the given
   * separator. Removes unnecessary separators and matches segments case-insensitively.
   *
   * @param base       The base network path.
   * @param path       The target path to resolve relative to the base.
   * @param separator  The file separator used by the operating system.
   * @param splitRegex The regular expression used to split the base path.
   * @return The resolved network path or null if no matching segment is found.
   */
  private static String resolveNetworkPath(String base, String path, String separator, String splitRegex) {
    // Normalize the base path by removing trailing separators
    while (base.endsWith(separator)) {
      base = base.substring(0, base.length() - 1);
    }

    // Split the base path by the appropriate separator
    String[] split = base.split(splitRegex);
    if (split.length > 0) {
      String segment = split[split.length - 1];

      // Case-insensitive match for both Windows and macOS
      if (path.toLowerCase().contains(segment.toLowerCase())) {
        // Extract the part of the path after the matched segment
        String extractedPath = path.toLowerCase().substring(path.toLowerCase().indexOf(segment.toLowerCase()) + segment.length());

        // Remove leading separators from extractedPath to avoid duplicate slashes
        extractedPath = extractedPath.replaceAll("^" + separator, "");

        // Concatenate the base and path with the correct separator
        return base + separator + extractedPath;
      }
    }

    // Return null if no matching segment is found
    return null;
  }

  public static String resolveWindowsNetworkPath(String base, String path) {
    try {
      String url = base;
      if (url == null) {
        return path;
      }

      while (url.endsWith("\\")) {
        url = url.substring(0, url.lastIndexOf("\\"));
      }

      String[] split = base.split("\\\\");
      if (split.length > 0) {
        String segment = split[split.length - 1];
        if (path.toLowerCase().contains(segment.toLowerCase())) {
          path = path.toLowerCase().substring(path.toLowerCase().indexOf(segment.toLowerCase()) + segment.length());

          return url + path;
        }
      }
      return null;
    }
    catch (Exception e) {
      LOG.error("Failed to resolve network path: " + e.getMessage(), e);
    }
    return path;
  }

  private static boolean isLocal() {
    return client.getSystemService().isLocal();
  }

}
