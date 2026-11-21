package de.mephisto.vpin.server.util;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import de.mephisto.vpin.restclient.util.OSUtil;
import org.apache.commons.lang3.Strings;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Window lookups through the Win32 API. JNA only implements window enumeration on Windows,
 * so on any other OS no window is ever reported as open.
 */
public class WindowsUtil {

  /**
   * Checks if a visible top-level window exists whose title contains one of the given strings (case-insensitive).
   * <p>
   * Called periodically while tables are played, so this enumerates the windows directly instead of using
   * JNA's WindowUtils#getAllWindows, which additionally opens the owning process of every window to resolve
   * its file path. GetWindowText (unlike GetWindowTextLength) does not send messages to windows of other
   * processes, so a hung window can't block this call.
   */
  public static boolean isProcessRunning(String... title) {
    if (!OSUtil.isWindows()) {
      return false;
    }
    AtomicBoolean found = new AtomicBoolean(false);
    char[] buffer = new char[512];
    User32.INSTANCE.EnumWindows((hwnd, data) -> {
      if (!User32.INSTANCE.IsWindowVisible(hwnd)) {
        return true;
      }
      int length = User32.INSTANCE.GetWindowText(hwnd, buffer, buffer.length);
      if (length <= 0) {
        return true;
      }
      String windowTitle = new String(buffer, 0, length);
      for (String s : title) {
        if (Strings.CI.contains(windowTitle, s)) {
          found.set(true);
          return false; //stop enumeration
        }
      }
      return true;
    }, null);
    return found.get();
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
