package de.mephisto.vpin.commons;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HMONITOR;
import com.sun.jna.platform.win32.WinUser.MONITORENUMPROC;
import com.sun.jna.platform.win32.WinUser.MONITORINFOEX;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.restclient.util.OSUtil;
import javafx.stage.Screen;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A small demo that tests the Win32 monitor API.
 * All available physical and virtual monitors are enumerated and
 * their capabilities printed to stdout
 *
 * @author Martin Steiger
 */
public class MonitorInfoUtil {

  private static boolean FORCE_USE_GRAPHICS_ENVIRONMENT = false;

  /**
   * List monitors
   *
   * @param args (ignored)
   */
  public static void main(String[] args) {
    List<MonitorInfo> monitors = getMonitors();

    System.out.println("Monitors: " + monitors.size());
    for (MonitorInfo monitor : monitors) {
      System.out.println(monitor.getName()
          + (monitor.isPrimary() ? " (Primary)" : "")
          + " : " + monitor.getX() + "," + monitor.getY()
          + " - " + monitor.getWidth() + "x" + monitor.getHeight()
          + " - " + monitor.isPortraitMode());
    }
  }

  public static MonitorInfo getPrimaryMonitor() {
    Optional<MonitorInfo> monitorInfo = getMonitors().stream().filter(m -> m.isPrimary()).findFirst();
    return monitorInfo.orElse(null);
  }


  public static List<MonitorInfo> getMonitors() {
    List<MonitorInfo> monitors = new ArrayList<>();
    if (OSUtil.isWindows() && !FORCE_USE_GRAPHICS_ENVIRONMENT) {

      int[] index = {1};
      User32.INSTANCE.EnumDisplayMonitors(null, null, new MONITORENUMPROC() {
        @Override
        public int apply(HMONITOR hMonitor, HDC hdc, RECT rect, LPARAM lparam) {
          MonitorInfo mon = enumerate(hMonitor, index[0]);
          monitors.add(mon);
          index[0]++;
          return 1;
        }
      }, new LPARAM(0));
    }
    else {
      int index = 1;
      GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
      for (GraphicsDevice gd : gds) {
        monitors.add(gdToMonitorInfo(gd, index));
        index++;
      }
    }

    // sort by xPosition
    monitors.sort((m1, m2) -> (int) (m1.getX() - m2.getX()));
    return monitors;
  }

  private static MonitorInfo enumerate(HMONITOR hMonitor, int index) {
    MonitorInfo monitor = new MonitorInfo();

    MONITORINFOEX info = new MONITORINFOEX();
    User32.INSTANCE.GetMonitorInfo(hMonitor, info);
    RECT screen = info.rcMonitor;
    //RECT workArea = info.rcWork;
    monitor.setX(screen.left);
    monitor.setY(screen.top);
    monitor.setWidth(screen.right - screen.left);
    monitor.setHeight(screen.bottom - screen.top);

    boolean isPrimary = (info.dwFlags & WinUser.MONITORINFOF_PRIMARY) != 0;
    monitor.setPrimary(isPrimary);
    if (isPrimary) {
      monitor.setScaling(Screen.getPrimary().getOutputScaleX());
    }
    else {
      List<Screen> screens = Screen.getScreens().stream().filter(s -> !Screen.getPrimary().equals(s)).collect(Collectors.toList());
      for (Screen s : screens) {
        if (s.getBounds().getMinX() == monitor.getX()) {
          monitor.setScaling(s.getOutputScaleX());
          break;
        }
      }
    }
    monitor.setPortraitMode(monitor.getWidth() < monitor.getHeight());

    String deviceName = new String(info.szDevice);
    monitor.setName(deviceName.trim());
    // index starts with 1
    monitor.setId(index);


    return monitor;
  }

  private static MonitorInfo gdToMonitorInfo(GraphicsDevice gd, int index) {
    MonitorInfo monitor = new MonitorInfo();

    java.awt.Rectangle bounds = gd.getDefaultConfiguration().getBounds();
    monitor.setX(bounds.x);
    monitor.setY(bounds.y);
    monitor.setWidth(bounds.width);
    monitor.setHeight(bounds.height);

    boolean isPrimary = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice() == gd;
    monitor.setPrimary(isPrimary);
    monitor.setPortraitMode(bounds.width < bounds.height);

    String deviceName = gd.getIDstring();
    if (deviceName != null) {
      deviceName = deviceName.replaceAll("\\\\", "").replaceAll("\\.", "");
    }
    monitor.setName(deviceName);

    monitor.setId(index);

    return monitor;
  }

}