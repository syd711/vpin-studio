package de.mephisto.vpin.server;

import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.server.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class VPinStudioServerTray {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioServerTray.class);

  public VPinStudioServerTray() {
    //Check the SystemTray is supported
    if (!SystemTray.isSupported()) {
      LOG.info("SystemTray is not supported");
      return;
    }
    final PopupMenu popup = new PopupMenu();
    final TrayIcon trayIcon = new TrayIcon(ResourceLoader.getResource("logo-small.png"));
    final SystemTray tray = SystemTray.getSystemTray();
    MenuItem restartItem = new MenuItem("Restart");
    restartItem.addActionListener(e -> {
      try {
        restartItem.setEnabled(false);
        new VPinStudioServerStateManager().restart();
      } catch (Exception ex) {
        LOG.error("Failed to restart VPin Studio Server: " + ex.getMessage());
      } finally {
        restartItem.setEnabled(true);
      }
    });
    MenuItem logsItem = new MenuItem("Show Logs");
    logsItem.addActionListener(e -> {
      try {
        File file = new File("./vpin-studio-server.log");
        if (file.exists()) {
          Desktop.getDesktop().open(file);
        }
      } catch (IOException ex) {
        LOG.error("Failed to open log file: " + ex.getMessage());
      }
    });
    MenuItem exitItem = new MenuItem("Terminate");
    exitItem.addActionListener(e -> System.exit(0));
//    popup.add(restartItem);
    popup.add(logsItem);
    popup.add(new MenuItem("Version " + SystemUtil.getVersion()));
    popup.addSeparator();
    popup.add(exitItem);

    trayIcon.setPopupMenu(popup);
    try {
      tray.add(trayIcon);
    } catch (AWTException e) {
      LOG.error("TrayIcon could not be added: " + e.getMessage(), e);
    }
  }
}
