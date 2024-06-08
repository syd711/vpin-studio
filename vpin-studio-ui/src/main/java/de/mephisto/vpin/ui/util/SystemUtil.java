package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.Studio;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static de.mephisto.vpin.ui.Studio.client;

public class SystemUtil {
  private final static Logger LOG = LoggerFactory.getLogger(SystemUtil.class);

  public static String publicUrl = null;

  static {
    UISettings jsonPreference = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    publicUrl = jsonPreference.getWinNetworkShare();
  }

  public static boolean isFolderActionSupported() {
    return isLocal() || (!StringUtils.isEmpty(publicUrl) && isWindows());
  }

  public static void openFolder(File folder) {
    openFolder(folder, null);
  }

  public static void openFolder(File folder, File fallback) {
    if (isLocal()) {
      try {
        if (folder.exists()) {
          new ProcessBuilder("explorer.exe", folder.getAbsolutePath()).start();
          return;
        }
        if (fallback.exists()) {
          new ProcessBuilder("explorer.exe", fallback.getAbsolutePath()).start();
        }
      } catch (IOException e) {
        LOG.error("Failed to open folder: " + e.getMessage(), e);
      }
    }
    else {
      if (isWindows()) {
        try {
          String path = folder.getAbsolutePath();

          String remotePath = resolveNetworkPath(publicUrl, path);
          if (remotePath != null) {
            new ProcessBuilder("explorer.exe", remotePath).start();
          }
        } catch (IOException e) {
          LOG.error("Failed to open network folder: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open network folder: " + e.getMessage());
        }
      }
    }
  }

  public static String resolveNetworkPath(String base, String path) {
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
    } catch (Exception e) {
      LOG.error("Failed to resolve network path: " + e.getMessage(), e);
    }
    return path;
  }

  private static boolean isLocal() {
    return client.getSystemService().isLocal();
  }

  public static boolean isWindows() {
    String os = System.getProperty("os.name");
    return os.contains("Windows");
  }
}
