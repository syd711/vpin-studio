package de.mephisto.vpin.server.util;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import de.mephisto.vpin.restclient.util.OSUtil;
import org.apache.commons.lang3.Strings;

import java.util.List;

/**
 * Window lookups through the Win32 API. JNA only implements window enumeration on Windows,
 * so on any other OS no window is ever reported as open.
 */
public class WindowsUtil {

  public static boolean isProcessRunning(String... title) {
    if (!OSUtil.isWindows()) {
      return false;
    }
    List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
    for (String s : title) {
      if (windows.stream().anyMatch(wdw -> Strings.CI.contains(wdw.getTitle(), s))) {
        return true;
      }
    }
    return false;
  }

  public static boolean isWindowOpened(String name) {
    return isProcessRunning(name);
  }

  /**
   * Waits up to 30 seconds for the window with the given title and brings it to the front.
   * Does nothing outside Windows.
   */
  public static void focusWindowAsync(String title) {
    if (!OSUtil.isWindows()) {
      return;
    }
    new Thread(() -> {
      Thread.currentThread().setName(title + " Focus Thread");
      long timeoutMs = 30000;
      long start = System.currentTimeMillis();

      while (System.currentTimeMillis() - start < timeoutMs) {
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, title);

        if (hwnd != null) {
          try { Thread.sleep(4000); } catch (InterruptedException e) { break; }
          User32.INSTANCE.ShowWindow(hwnd, 9); // SW_RESTORE
          User32.INSTANCE.SetForegroundWindow(hwnd);

          try { Thread.sleep(4000); } catch (InterruptedException e) { break; }
          User32.INSTANCE.ShowWindow(hwnd, 9); // SW_RESTORE
          User32.INSTANCE.SetForegroundWindow(hwnd);
          return;
        }

        try { Thread.sleep(500); } catch (InterruptedException e) { break; }
      }
    }).start();
  }
}
