package de.mephisto.vpin.commons;

import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.restclient.util.OSUtil;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public class MonitorInfoUtil {
  private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final static List<MonitorInfo> monitors = new ArrayList<>();

  static {
    try {
      List<MonitorInfo> monitorsGde = getMonitors(true);
      logScreenSummary("GDE Monitor List", monitorsGde);
      List<MonitorInfo> monitorsJna = getMonitors(false);
      logScreenSummary("JNA Monitor List", monitorsJna);

      if (monitorsJna.size() > monitorsGde.size()) {
        LOG.info("Using JNA Monitors");
        monitors.addAll(monitorsJna);
      }
      else {
        LOG.info("Using GDE Monitors");
        monitors.addAll(monitorsGde);
      }
    }
    catch (Exception e) {
      LOG.error("Monitor initialization failed: {}", e.getMessage());
      monitors.addAll(getMonitors(true));
    }
  }

  /**
   * List monitors
   *
   * @param args (ignored)
   */
  public static void main(String[] args) {
    List<MonitorInfo> monitors = getMonitors(true);
    for (MonitorInfo monitor : monitors) {
      System.out.println(monitor.toDetailsString());
    }
    System.out.println("---------------------------");
    monitors = getMonitors(false);
    for (MonitorInfo monitor : monitors) {
      System.out.println(monitor.toDetailsString());
    }
  }

  public static MonitorInfo getPrimaryMonitor() {
    Optional<MonitorInfo> monitorInfo = getMonitors().stream().filter(MonitorInfo::isPrimary).findFirst();
    return monitorInfo.orElse(null);
  }

  public static List<MonitorInfo> getMonitors() {
    return monitors;
  }

  private static List<MonitorInfo> getMonitors(boolean useGraphicsEnv) {
    List<MonitorInfo> monitors = new ArrayList<>();
    if (OSUtil.isWindows() && !useGraphicsEnv) {
      monitors.addAll(JNAMonitorUtil.getMonitors());
    }
    else {
      if (GraphicsEnvironment.isHeadless()) {
        return Collections.emptyList();
      }

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

  private static void logScreenSummary(String names, List<MonitorInfo> monitors) {
    try {
      LOG.info("########################## " + names + " #####################################");
      for (MonitorInfo monitor : monitors) {
        LOG.info(monitor.toDetailsString());
      }
      LOG.info("######################### /" + names + " #####################################");
    }
    catch (Exception e) {
      LOG.error("Logging monitor information failed: {}", e.getMessage());
    }
  }
}