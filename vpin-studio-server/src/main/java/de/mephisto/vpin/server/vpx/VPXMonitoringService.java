package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameStatusService;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.popper.TableStatusChangedOrigin;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.util.SystemUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
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
  private PopperService popperService;

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
      String tableName = getVPXTableName();
      if (tableName != null) {
        notifyTableStart(tableName);
      }
      else {
        notifyTableEnd();
      }
    }
    catch (Exception e) {
      LOG.info("VPX Monitor Thread failed: " + e.getMessage(), e);
    }
  }

  private void notifyTableEnd() {
    if (gameStatusService.getStatus().isActive()) {
      int gameId = gameStatusService.getStatus().getGameId();
      Game game = gameService.getGame(gameId);
      LOG.info(this.getClass().getSimpleName() + " notifying table end event of \"" + game.getGameDisplayName() + "\"");
      popperService.notifyTableStatusChange(game, false, TableStatusChangedOrigin.ORIGIN_VPS);
    }
  }

  private void notifyTableStart(@NonNull String tableName) {
    if (!gameStatusService.getStatus().isActive()) {
      LOG.info("Detected VPX running with table \"" + tableName + "\", resolving game for it.");
      Game game = gameService.getGameByName(tableName);
      if (game != null) {
        LOG.info(this.getClass().getSimpleName() + " notifying table start event of \"" + game.getGameDisplayName() + "\"");
        popperService.notifyTableStatusChange(game, true, TableStatusChangedOrigin.ORIGIN_VPS);
      }
      else {
        LOG.info(this.getClass().getSimpleName() + " registered a VPX window, but the game could not be resolved for name \"" + tableName + "\"");
      }
    }
  }

  private String getVPXTableName() {
    List<String> allWindowNames = SystemUtil.getAllWindowNames();
    boolean playerRunning = allWindowNames.stream().anyMatch(name -> name.contains("Visual Pinball Player"));
    for (String name : allWindowNames) {
      if (playerRunning && name.toLowerCase().contains("Pinball".toLowerCase())) {
        if (name.contains("[") && name.contains("]")) {
          return name.substring(name.indexOf("[") + 1, name.length() - 1);
        }
      }
    }
    return null;
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
        }
        else {
          running.set(false);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to update VPX monitoring: " + e.getMessage(), e);
    }
  }
}
