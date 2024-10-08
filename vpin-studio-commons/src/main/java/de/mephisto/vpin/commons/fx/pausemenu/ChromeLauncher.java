package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.NirCmd;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChromeLauncher {
  private final static Logger LOG = LoggerFactory.getLogger(ChromeLauncher.class);

  private static boolean launched = false;
  private static boolean kioskMode = false;
  private static boolean useToTop = true;

  public static void showYouTubeVideo(FrontendPlayerDisplay screenDisplay, String url, String title) {
    launched = true;
    try {
      int x = screenDisplay.getX();
      int y = screenDisplay.getY();
      int width = screenDisplay.getWidth();
      int height = screenDisplay.getHeight();
      LOG.info("Showing Chrome at " + x + "/" + y + " in [" + width + "x" + height + "]");
      File chromeExe = new File("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
      if (!chromeExe.exists()) {
        chromeExe = new File("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");
        if (!chromeExe.exists()) {
          LOG.error("Chrome installation not found: " + chromeExe.getAbsolutePath());
          return;
        }
      }

      File profileFolder = new File("./resources/menu-profile");
      profileFolder.mkdirs();

      String resizing = "";
      if (!kioskMode) {
        resizing = "window.resizeTo(" + width + "," + height + ")";
      }

      List<String> cmds = Arrays.asList("\"" + chromeExe.getAbsolutePath() + "\"",
        "--app=\"data:text/html,<html><head><title>vpin</title></head><body><script>" + resizing + ";window.location='" + url + "';</script></body></html>\"",
        "--window-position=" + x + "," + y, "--user-data-dir=\"" + profileFolder.getAbsolutePath() + "\"", "--autoplay-policy=no-user-gesture-required");
      if (kioskMode) {
        cmds = new ArrayList<>(cmds);
        cmds.add("--kiosk");
      }

      String commandString = String.join(" ", cmds);
      LOG.info("Chrome Command: " + commandString);

      SystemCommandExecutor executor = new SystemCommandExecutor(cmds, false);
      executor.setDir(chromeExe.getParentFile());
      executor.enableLogging(true);
      executor.executeCommandAsync();
      LOG.info(String.join(" ", cmds));

      if (useToTop) {
        new Thread(() -> {
          try {
            Thread.sleep(ServerFX.TO_FRONT_DELAY - 500);
            NirCmd.setTopMost(title);
          } catch (InterruptedException e) {
            //ignore
          }
        }).start();
      }

      ServerFX.toFront(PauseMenu.stage, true);
    } catch (Exception e) {
      LOG.error("Failed to show YT video: " + e.getMessage(), e);
    }
  }

  public static void exitBrowser() {
    try {
      if (launched) {
        SystemCommandExecutor exit = new SystemCommandExecutor(Arrays.asList("taskkill", "/IM", "chrome.exe"), false);
        exit.executeCommand();
      }
    } catch (Exception e) {
      LOG.error("Failed to exit browser: " + e.getMessage());
    }
  }
}
