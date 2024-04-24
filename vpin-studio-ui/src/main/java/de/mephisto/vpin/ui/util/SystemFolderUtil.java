package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.Studio;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static de.mephisto.vpin.ui.Studio.client;

public class SystemFolderUtil {
  private final static Logger LOG = LoggerFactory.getLogger(SystemFolderUtil.class);

  private static String publicUrl = "\\\\localhost\\vPinball";

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

          String[] split = publicUrl.split("\\\\");
          if (split.length > 0) {
            String segment = split[split.length - 1];
            if (path.contains(segment)) {
              path = path.substring(path.indexOf(segment) + segment.length());

              String remotePath = publicUrl + path;
              new ProcessBuilder("explorer.exe", remotePath).start();
            }
          }
        } catch (IOException e) {
          LOG.error("Failed to open network folder: " + e.getMessage(), e);
          WidgetFactory.showAlert(Studio.stage, "Error", "Failed to open network folder: " + e.getMessage());
        }
      }
    }
  }

  private static boolean isLocal() {
    return client.getSystemService().isLocal();
  }

  private static boolean isWindows() {
    String os = System.getProperty("os.name");
    return os.contains("Windows");
  }
}
