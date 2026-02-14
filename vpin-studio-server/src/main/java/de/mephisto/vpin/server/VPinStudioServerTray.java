package de.mephisto.vpin.server;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.server.util.VersionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;

public class VPinStudioServerTray {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
        VPinStudioClient client = new VPinStudioClient("localhost");
        client.getSystemService().restart();
      }
      catch (Exception ex) {
        LOG.error("Failed to restart VPin Studio Server: " + ex.getMessage());
      }
      finally {
        restartItem.setEnabled(true);
      }
    });
    MenuItem launchItem = new MenuItem("Launch Studio");
    launchItem.addActionListener(e -> {
      launchStudio();
    });
    MenuItem logsItem = new MenuItem("Show Logs");
    logsItem.addActionListener(e -> {
      try {
        File file = new File("./vpin-studio-server.log");
        if (file.exists()) {
          Desktop.getDesktop().open(file);
        }
      }
      catch (IOException ex) {
        LOG.error("Failed to open log file: " + ex.getMessage());
      }
    });
    MenuItem exitItem = new MenuItem("Terminate");
    exitItem.addActionListener(e -> System.exit(0));
    popup.add(launchItem);
    popup.add(logsItem);
    popup.add(new MenuItem("Version " + VersionUtil.getVersion()));
    popup.addSeparator();
    popup.add(restartItem);
    popup.addSeparator();
    popup.add(exitItem);

    trayIcon.addMouseListener(new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          launchStudio();
        }
      }

      @Override
      public void mousePressed(MouseEvent e) {

      }

      @Override
      public void mouseReleased(MouseEvent e) {

      }

      @Override
      public void mouseEntered(MouseEvent e) {

      }

      @Override
      public void mouseExited(MouseEvent e) {

      }
    });

    trayIcon.setPopupMenu(popup);
    try {
      tray.add(trayIcon);
    }
    catch (AWTException e) {
      LOG.error("TrayIcon could not be added: " + e.getMessage(), e);
    }
  }

  private static void launchStudio() {
    try {
      File file = new File("./VPin-Studio.exe");
      if (file.exists()) {
        SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("VPin-Studio.exe"));
        executor.setDir(new File("./"));
        executor.executeCommandAsync();
      }
      else {
        LOG.error("VPin-Studio.exe not found in directory " + new File("./").getAbsolutePath());
      }
    }
    catch (Exception ex) {
      LOG.error("Failed to execute Studio: " + ex.getMessage(), ex);
    }
  }
}
