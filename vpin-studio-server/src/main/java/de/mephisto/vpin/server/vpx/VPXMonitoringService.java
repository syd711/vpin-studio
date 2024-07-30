package de.mephisto.vpin.server.vpx;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class VPXMonitoringService implements InitializingBean, PreferenceChangedListener, Runnable {
  private final static Logger LOG = LoggerFactory.getLogger(VPXMonitoringService.class);
  private final AtomicBoolean running = new AtomicBoolean(false);

  @Autowired
  private GameStatusService gameStatusService;

  @Autowired
  private GameService gameService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private PreferencesService preferencesService;

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  @Override
  public void run() {
    try {
      if (!running.get()) {
        return;
      }
      Thread.currentThread().setName("VPX Monitor Thread");

      List<GameEmulator> emulators = frontendStatusService.getGameEmulators();

      List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
      boolean playerRunning = windows.stream().anyMatch(wdw -> StringUtils.containsIgnoreCase(wdw.getTitle(), "Visual Pinball Player"));

      if (playerRunning && !gameStatusService.getStatus().isActive()) {
        int emuId = -1;
        String tableName = null;

        for (DesktopWindow wdw : windows) {
          for (GameEmulator emu : emulators) {
            if (StringUtils.startsWithIgnoreCase(wdw.getFilePath(), emu.getInstallationDirectory())) {
              String windowTitle = wdw.getTitle();
              //LOG.info("VPX process detected with window title " + wdw.getTitle());
              if (windowTitle.contains("[") && windowTitle.contains("]")) {
                emuId = emu.getId();
                tableName = windowTitle.substring(windowTitle.indexOf("[") + 1, windowTitle.length() - 1);
              }
            }
          }
        }
        if (tableName != null) {
          notifyTableStartByFileName(emuId, tableName);
        }
      }
      else if (!playerRunning) {
        notifyTableEnd();
      }
    }
    catch (Exception e) {
      LOG.info("VPX Monitor Thread failed: " + e.getMessage(), e);
    }
  }

  private void notifyTableEnd() {
    int gameId = gameStatusService.getStatus().getGameId();
    if (gameId > 0) {
      Game game = gameService.getGame(gameId);
      LOG.info(this.getClass().getSimpleName() + " notifying table end event of \"" + game.getGameDisplayName() + "\"");
      frontendStatusService.notifyTableStatusChange(game, false, TableStatusChangedOrigin.ORIGIN_VPS);
    }
  }

  private void notifyTableStartByFileName(int emuId, @NonNull String tableName) {
    LOG.info("Detected VPX running with table filename \"" + tableName + ".vpx\", resolving game for it.");

    Game game = gameService.getGameByFilename(emuId, tableName + ".vpx");
    if (game != null) {
      LOG.info(this.getClass().getSimpleName() + " notifying table start event of \"" + game.getGameDisplayName() + "\"");
      frontendStatusService.notifyTableStatusChange(game, true, TableStatusChangedOrigin.ORIGIN_VPS);
    }
    else {
      LOG.info(this.getClass().getSimpleName() + " registered a VPX window, but the game could not be resolved for name \"" + tableName + "\"");
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (Features.VPX_MONITORING) {
      scheduler.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
      preferencesService.addChangeListener(this);
      preferenceChanged(PreferenceNames.SERVER_SETTINGS, null, null);
    }
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    try {
      if (PreferenceNames.SERVER_SETTINGS.equalsIgnoreCase(propertyName)) {
        ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
        if (serverSettings.isUseVPXTableMonitor()) {
          running.set(true);
          LOG.info("Enabled VPX Monitor");
        }
        else {
          running.set(false);
          LOG.info("Disabled VPX Monitor");
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to update VPX monitoring: " + e.getMessage(), e);
    }
  }
}
