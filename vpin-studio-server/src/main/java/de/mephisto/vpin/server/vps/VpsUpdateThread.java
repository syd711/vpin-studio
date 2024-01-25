package de.mephisto.vpin.server.vps;

import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VpsUpdateThread extends Thread {
  private final static Logger LOG = LoggerFactory.getLogger(VpsUpdateThread.class);

  private final PreferencesService preferencesService;

  public VpsUpdateThread(PreferencesService preferencesService) {
    super("VPS Updater");
    this.preferencesService = preferencesService;
  }

  @Override
  public void run() {
    try {
      int intervalMin = (int) preferencesService.getPreferenceValue(PreferenceNames.DISCORD_VPS_REFRESH_INTERVAL_MIN);
      if (intervalMin <= 0) {
        intervalMin = 30;
      }

      VPS.getInstance().update();
      Thread.sleep((long) intervalMin * 60 * 1000);
    } catch (Exception e) {
      LOG.error("Error in " + this + ": " + e.getMessage(), e);
    }
  }
}
