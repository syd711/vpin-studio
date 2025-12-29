package de.mephisto.vpin.commons;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.restclient.util.OSUtil;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public class MonitorInfoUtil {
  private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final boolean FORCE_USE_GRAPHICS_ENVIRONMENT = true;

  /**
   * List monitors
   *
   * @param args (ignored)
   */
  public static void main(String[] args) {
    List<MonitorInfo> monitors = getMonitors();
    for (MonitorInfo monitor : monitors) {
      System.out.println(monitor);
    }
  }

  public static MonitorInfo getPrimaryMonitor() {
    Optional<MonitorInfo> monitorInfo = getMonitors().stream().filter(MonitorInfo::isPrimary).findFirst();
    return monitorInfo.orElse(null);
  }


  public static List<MonitorInfo> getMonitors() {
    List<MonitorInfo> monitors = new ArrayList<>();
    if (OSUtil.isWindows() && !FORCE_USE_GRAPHICS_ENVIRONMENT) {
      //TODO only for legacy comparison, remove this
      int[] index = {1};
      User32.INSTANCE.EnumDisplayMonitors(null, null, new WinUser.MONITORENUMPROC() {
        @Override
        public int apply(WinUser.HMONITOR hMonitor, WinDef.HDC hdc, WinDef.RECT rect, WinDef.LPARAM lparam) {
          MonitorInfo mon = enumerate(hMonitor, rect, index[0]);
          monitors.add(mon);
          index[0]++;
          return 1;
        }
      }, new WinDef.LPARAM(0));
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

  //TODO only for legacy comparison, remove this
  private static MonitorInfo enumerate(WinUser.HMONITOR hMonitor, WinDef.RECT rect, int index) {
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
//    if (isPrimary) {
//      monitor.setScaling(Screen.getPrimary().getOutputScaleX());
//    }
//    else {
//      List<Screen> screens = Screen.getScreens().stream().filter(s -> !Screen.getPrimary().equals(s)).collect(Collectors.toList());
//      for (Screen s : screens) {
//        double screenX = s.getBounds().getMinX();
//        if (s.getOutputScaleX() > 0) {
//          screenX = screenX * s.getOutputScaleX();
//        }
//        if (screenX == monitor.getX()) {
//          monitor.setScaling(s.getOutputScaleX());
//          break;
//        }
//      }
//    }
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
    monitor.setMinY(bounds.getMinY());

    AffineTransform tx = gd.getDefaultConfiguration().getDefaultTransform();
    monitor.setScaling(tx.getScaleX());

    if (tx.getScaleX() > 1) {
      monitor.setWidth((int) (bounds.width * tx.getScaleX()));
      monitor.setHeight((int) (bounds.height * tx.getScaleY()));
    }

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

  public static void logScreenSummary() {
    try {
      LOG.info("########################## Screen Summary #####################################");
      List<MonitorInfo> monitors = MonitorInfoUtil.getMonitors();
      for (MonitorInfo monitor : monitors) {
        LOG.info(monitor.toDetailsString());
      }
      LOG.info("######################### /Screen Summary #####################################");
    }
    catch (Exception e) {
      LOG.error("Logging monitor information failed: {}", e.getMessage());
    }
  }
}