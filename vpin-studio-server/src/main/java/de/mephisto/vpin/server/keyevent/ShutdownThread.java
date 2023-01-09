package de.mephisto.vpin.server.keyevent;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class ShutdownThread extends Thread {
  private final static Logger LOG = LoggerFactory.getLogger(ShutdownThread.class);

  private PreferencesService preferencesService;
  private int idleMinutes;

  public ShutdownThread(PreferencesService preferencesService) {
    this.preferencesService = preferencesService;
  }

  public void run() {
    Thread.currentThread().setName("System Shutdown Listener");
    LOG.info("Started " + Thread.currentThread().getName() + " (" + preferencesService.getPreferenceValue(PreferenceNames.IDLE_TIMEOUT) + " minutes timeout)");
    while (true) {
      try {
        Thread.sleep(60 * 1000);
        idleMinutes += 1;

        Object preferenceValue = preferencesService.getPreferenceValue(PreferenceNames.IDLE_TIMEOUT);
        if (preferenceValue != null) {
          int idlePreference = Integer.parseInt(String.valueOf(preferenceValue));
          if (idlePreference > 0) {
            LOG.info("Current timeout minutes: " + idleMinutes + " of " + preferenceValue);
          }

          if (idlePreference > 0 && idlePreference <= idleMinutes) {
            LOG.error("Executing shutdown after being idle for " + idleMinutes + " minutes");
            shutdown();
          }
        }
      } catch (InterruptedException e) {
        LOG.error("Error in shutdown thread: " + e.getMessage(), e);
      }
    }
  }

  public void notifyKeyEvent() {
    this.idleMinutes = 0;
  }

  private void shutdown() {
    try {
      SystemCommandExecutor executor = new SystemCommandExecutor(Arrays.asList("shutdown", "-s"));
      executor.executeCommand();
    } catch (Exception e) {
      LOG.error("Error executing shutdown: " + e.getMessage(), e);
    }
  }

  public void reset() {
    idleMinutes = 0;
  }
}
