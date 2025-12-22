package de.mephisto.vpin.commons;

import de.mephisto.vpin.restclient.system.MonitorInfo;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public class MonitorInfoUtil {

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
    int index = 1;
    GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    for (GraphicsDevice gd : gds) {
      monitors.add(gdToMonitorInfo(gd, index));
      index++;
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
}