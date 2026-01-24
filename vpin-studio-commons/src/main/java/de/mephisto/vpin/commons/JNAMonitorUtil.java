package de.mephisto.vpin.commons;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;
import de.mephisto.vpin.restclient.system.MonitorInfo;

import java.util.ArrayList;
import java.util.List;

public class JNAMonitorUtil {
  private static int index = 1;
  // ===== CONSTANTS =====
  // MONITOR_DPI_TYPE enum values (from ShellScalingApi.h)
  public static final int MDT_EFFECTIVE_DPI = 0;  // Effective DPI for user (recommended)

  // Define SHCore library interface with GetDpiForMonitor
  public interface SHCore extends com.sun.jna.Library {
    SHCore INSTANCE = Native.load("shcore", SHCore.class,
        W32APIOptions.DEFAULT_OPTIONS);

    // HRESULT GetDpiForMonitor(
    //   HMONITOR         hmonitor,
    //   MONITOR_DPI_TYPE dpiType,
    //   UINT             *dpiX,
    //   UINT             *dpiY
    // );
    int GetDpiForMonitor(WinUser.HMONITOR hmonitor, int dpiType,
                         IntByReference dpiX, IntByReference dpiY);
  }

  // Extended GDI32 interface with GetDeviceCaps
  public interface GDI32 extends com.sun.jna.Library {
    GDI32 INSTANCE = Native.load("gdi32", GDI32.class, W32APIOptions.DEFAULT_OPTIONS);

    WinDef.HDC CreateDC(String lpszDriver, String lpszDevice, String lpszOutput, Pointer lpInitData);

    int GetDeviceCaps(WinDef.HDC hdc, int index);
  }

  private static final int DEFAULT_DPI = 96;

  public static List<MonitorInfo> getMonitors() {
    index = 1;
    List<MonitorInfo> monitors = new ArrayList<>();
    // Get a desktop DC first
    WinDef.HDC desktopDC = User32.INSTANCE.GetDC(null);

    try {
      User32.INSTANCE.EnumDisplayMonitors(desktopDC, null, new WinUser.MONITORENUMPROC() {
        @Override
        public int apply(WinUser.HMONITOR hMonitor, WinDef.HDC hdc, WinDef.RECT rect, WinDef.LPARAM lparam) {
          MonitorInfo mon = enumerate(hMonitor, hdc, rect, index);
          monitors.add(mon);
          index++;
          return 1;
        }
      }, new WinDef.LPARAM(0));
    }
    finally {
      // Don't forget to release!
      User32.INSTANCE.ReleaseDC(null, desktopDC);
    }
    return monitors;
  }

  /**
   * Get DPI for a specific monitor using HMONITOR handle
   * This is the CORRECT way to get per-monitor DPI
   */
  public static double getMonitorDPI(WinUser.HMONITOR hMonitor) {
    IntByReference dpiX = new IntByReference();
    IntByReference dpiY = new IntByReference();

    try {
      int result = SHCore.INSTANCE.GetDpiForMonitor(
          hMonitor, MDT_EFFECTIVE_DPI, dpiX, dpiY);

      if (result == 0) { // S_OK
        double dpiXv = dpiX.getValue();
        double scalingX = dpiXv / (double) DEFAULT_DPI;
        return scalingX;
      }
    }
    catch (UnsatisfiedLinkError e) {
      // GetDpiForMonitor not available (Windows 7 or earlier)
      System.err.println("GetDpiForMonitor not available. Windows 8.1+ required for per-monitor DPI.");
    }
    catch (Exception e) {
      System.err.println("Error getting DPI: " + e.getMessage());
    }
    return 1;
  }

  private static MonitorInfo enumerate(WinUser.HMONITOR hMonitor, WinDef.HDC hdc, WinDef.RECT rect, int index) {
    MonitorInfo monitor = new MonitorInfo();

    WinUser.MONITORINFOEX info = new WinUser.MONITORINFOEX();
    User32.INSTANCE.GetMonitorInfo(hMonitor, info);
    //RECT workArea = info.rcWork;
    monitor.setX(rect.left);
    monitor.setY(rect.top);
    monitor.setWidth(rect.right - rect.left);
    monitor.setHeight(rect.bottom - rect.top);

    boolean isPrimary = (info.dwFlags & WinUser.MONITORINFOF_PRIMARY) != 0;
    monitor.setPrimary(isPrimary);
    monitor.setPortraitMode(monitor.getWidth() < monitor.getHeight());

    String deviceName = new String(info.szDevice);
    monitor.setScaling(getMonitorDPI(hMonitor));

    monitor.setName(deviceName.trim());
    // index starts with 1
    monitor.setId(index);


    return monitor;
  }
}
