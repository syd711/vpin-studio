package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.OverlayWindowFX;
import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.popper.PinUPPlayerDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ChromeLauncher {
  private final static Logger LOG = LoggerFactory.getLogger(ChromeLauncher.class);

  public static void showYouTubeVideo(PinUPPlayerDisplay screenDisplay, String url) {
    try {
      int x = screenDisplay.getX();
      int y = screenDisplay.getY();
      int width = screenDisplay.getWidth();
      int height = screenDisplay.getHeight();
      LOG.info("Showing Chrome at " + x + "/" + y + " in [" + width + "x" + height + "]");
      File chromeExe = new File("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
      if (!chromeExe.exists()) {
        LOG.error("Chrome installation not found: " + chromeExe.getAbsolutePath());
        return;
      }

      File profileFolder = new File("./resources/menu-profile");
      profileFolder.mkdirs();
      List<String> cmds = Arrays.asList("\"C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe\"",
        "--app=\"data:text/html,<html><body><script>window.resizeTo(" + width + "," + height + ");window.location='" + url + "';</script></body></html>\"",
        "--window-position=" + x + "," + y, "--user-data-dir=" + profileFolder.getAbsolutePath(), "--autoplay-policy=no-user-gesture-required");
      SystemCommandExecutor executor = new SystemCommandExecutor(cmds, false);
      executor.executeCommandAsync();
      LOG.info(String.join(" ", cmds));

      OverlayWindowFX.toFront(PauseMenu.stage, true);
    } catch (Exception e) {
      LOG.error("Failed to show YT video: " + e.getMessage(), e);
    }
  }

  public static void exitBrowser() {
    try {
      SystemCommandExecutor exit = new SystemCommandExecutor(Arrays.asList("taskkill", "/IM", "chrome.exe"), false);
      exit.executeCommand();
    } catch (Exception e) {
      LOG.error("Failed to exit browser: " + e.getMessage());
    }
  }
}