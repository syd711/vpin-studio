package de.mephisto.vpin.server.util;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import de.mephisto.vpin.commons.utils.NirCmd;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class WindowsUtil {

  public static boolean isProcessRunning(String... title) {
    List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
    for (String s : title) {
      if (windows.stream().anyMatch(wdw -> StringUtils.containsIgnoreCase(wdw.getTitle(), s))) {
        return true;
      }
    }
    return false;
  }
}
