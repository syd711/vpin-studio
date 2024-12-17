package de.mephisto.vpin.commons;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.List;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HMONITOR;
import com.sun.jna.platform.win32.WinUser.MONITORENUMPROC;
import com.sun.jna.platform.win32.WinUser.MONITORINFOEX;

import de.mephisto.vpin.restclient.util.OSUtil;

/**
 * A small demo that tests the Win32 monitor API.
 * All available physical and virtual monitors are enumerated and
 * their capabilities printed to stdout
 * @author Martin Steiger
 */
public class MonitorInfoUtil
{
  /**
   * List monitors
   * @param args (ignored)
   */
  public static void main(String[] args) {
    List<MonitorInfo> monitors = getMonitors();

    System.out.println("Monitors: " + monitors.size());
    for (MonitorInfo monitor : monitors) {
      System.out.println(monitor.getDeviceName() 
          + (monitor.isPrimary() ? " (Primary)" : "")
          + " : " + monitor.getScreenX()+","+monitor.getScreenY() 
          + " - " + monitor.getScreenWidth() + "x" + monitor.getScreenHeight());
    }
  }

  public static List<MonitorInfo> getMonitors() {
    List<MonitorInfo> monitors = new ArrayList<>();

    if (OSUtil.isWindows()) {
      User32.INSTANCE.EnumDisplayMonitors(null, null, new MONITORENUMPROC() {
        @Override
        public int apply(HMONITOR hMonitor, HDC hdc, RECT rect, LPARAM lparam) {
          monitors.add(enumerate(hMonitor));
            return 1;
        }
      }, new LPARAM(0));
    }
    else {
      GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
      for (GraphicsDevice gd : gds) {
        monitors.add(gdToMonitorInfo(gd));
      }
    }
    return monitors;
  }

  private static MonitorInfo enumerate(HMONITOR hMonitor) {
    MonitorInfo monitor = new MonitorInfo();

    MONITORINFOEX info = new MONITORINFOEX();
    User32.INSTANCE.GetMonitorInfo(hMonitor, info);
    RECT screen = info.rcMonitor;
    //RECT workArea = info.rcWork;
    monitor.setScreenX(screen.left);
    monitor.setScreenY(screen.top);
    monitor.setScreenWidth(screen.right - screen.left);
    monitor.setScreenHeight(screen.bottom - screen.top);
    
    boolean isPrimary = (info.dwFlags & WinUser.MONITORINFOF_PRIMARY) != 0;
    monitor.setPrimary(isPrimary);

    String deviceName = new String(info.szDevice);
    monitor.setDeviceName(deviceName.trim());
    return monitor;
  }

  private static MonitorInfo gdToMonitorInfo(GraphicsDevice gd) {
    MonitorInfo monitor = new MonitorInfo();

    java.awt.Rectangle bounds = gd.getDefaultConfiguration().getBounds();
    monitor.setScreenX(bounds.x);
    monitor.setScreenY(bounds.y);
    monitor.setScreenWidth(bounds.width);
    monitor.setScreenHeight(bounds.height);
    
    boolean isPrimary = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice() == gd;
    monitor.setPrimary(isPrimary);

    String deviceName = gd.getIDstring();
    monitor.setDeviceName(deviceName);
    return monitor;
  }

}