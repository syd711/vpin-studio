package de.mephisto.vpin.server.inputs;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.commons.fx.pausemenu.PauseMenu;
import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.jobs.JobQueue;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ShutdownThread extends Thread {
  private final static Logger LOG = LoggerFactory.getLogger(ShutdownThread.class);

  private final PreferencesService preferencesService;
  private final JobQueue queue;
  private int idleMinutes;
  private boolean running = true;

  public ShutdownThread(PreferencesService preferencesService, JobQueue queue) {
    this.preferencesService = preferencesService;
    this.queue = queue;
  }

  public void run() {
    Thread.currentThread().setName("System Shutdown Listener");
    LOG.info("Started " + Thread.currentThread().getName() + " (" + preferencesService.getPreferenceValue(PreferenceNames.IDLE_TIMEOUT) + " minutes timeout)");
    while (running) {
      try {
        Thread.sleep(60 * 1000);

        boolean uiRunning = ServerFX.getInstance().isOverlayVisible() || PauseMenu.getInstance().isVisible();
        if (uiRunning) {
          idleMinutes = 0;
          continue;
        }

        idleMinutes += 1;

        Object preferenceValue = preferencesService.getPreferenceValue(PreferenceNames.IDLE_TIMEOUT);
        if (preferenceValue != null) {
          int idlePreference = Integer.parseInt(String.valueOf(preferenceValue));
          if (idlePreference > 0) {
            LOG.info("Current timeout minutes: " + idleMinutes + " of " + preferenceValue);
          }

          if (idlePreference > 0 && idlePreference <= idleMinutes) {
            if (!queue.isEmpty()) {
              LOG.info("Cancelled shutdown, because job queue is still executing " + queue.size() + " jobs.");
            }
            else {
              LOG.info("Executing shutdown after being idle for " + idleMinutes + " minutes");
              shutdownSystem();
            }
          }
        }
      }
      catch (InterruptedException e) {
        LOG.error("Error in shutdown thread: " + e.getMessage());
      }
    }
  }

  public void notifyKeyEvent() {
    this.idleMinutes = 0;
  }

  public void shutdown() {
    try {
      running = false;
      this.interrupt();
    }
    catch (Exception e) {
      LOG.warn("Unsafe shutdown listener shutdown: {}", e.getMessage());
    }
  }

  public static void shutdownSystem() {
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("shutdown", "-s"));
      executor.executeCommand();
    }
    catch (Exception e) {
      LOG.error("Error executing shutdown: " + e.getMessage(), e);
    }
  }

  public void reset() {
    idleMinutes = 0;
  }
}
