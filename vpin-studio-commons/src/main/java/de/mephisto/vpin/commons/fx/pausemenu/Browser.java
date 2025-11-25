package de.mephisto.vpin.commons.fx.pausemenu;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.utils.NirCmd;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Browser {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected static boolean launched = false;
  protected static boolean kioskMode = false;
  protected static boolean useToTop = true;

  public static Browser getInstance() {
    Browser b = new EdgeBrowser();
    if (b.getBrowserExe() == null || !b.getBrowserExe().exists()) {
      b = new ChromeBrowser();
    }
    if (!b.getBrowserExe().exists()) {
      LOG.error("No valid browser installation found!");
    }
    return b;
  }

  public void showYouTubeVideo(FrontendPlayerDisplay screenDisplay, String url, String title) {
    launched = true;
    try {
      int x = screenDisplay.getX();
      int y = screenDisplay.getY();
      int width = screenDisplay.getWidth();
      int height = screenDisplay.getHeight();
      LOG.info("Showing {} at " + x + "/" + y + " in [" + width + "x" + height + "]", this.getClass().getSimpleName());
      File chromeExe = getBrowserExe();
      if (chromeExe == null) return;

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
      SystemCommandExecutor executor = new SystemCommandExecutor(cmds, false);
      executor.setDir(chromeExe.getParentFile());
      executor.enableLogging(true);
      executor.executeCommandAsync();

      if (useToTop) {
        new Thread(() -> {
          try {
            Thread.sleep(ServerFX.TO_FRONT_DELAY - 500);
            NirCmd.setTopMost(title);
            NirCmd.setTopMost("Chrome");
            NirCmd.setTopMost("Edge");
            Thread.sleep(1000);
            NirCmd.setTopMost("Chrome");
            NirCmd.setTopMost("Edge");
          }
          catch (InterruptedException e) {
            //ignore
          }
        }).start();
      }

      ServerFX.toFront(PauseMenu.stage, true);
    }
    catch (Exception e) {
      LOG.error("Failed to show YT video: " + e.getMessage(), e);
    }
  }

  public abstract File getBrowserExe();

  public void exitBrowser() {
    try {
      if (launched) {
        String exeName = getBrowserExe().getName();
        SystemCommandExecutor exit = new SystemCommandExecutor(Arrays.asList("taskkill", "/IM", exeName), false);
        exit.executeCommand();
      }
    }
    catch (Exception e) {
      LOG.error("Failed to exit browser: " + e.getMessage());
    }
  }
}
