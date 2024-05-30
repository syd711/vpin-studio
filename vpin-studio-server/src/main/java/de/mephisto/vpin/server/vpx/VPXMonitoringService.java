package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameStatusService;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.util.SystemUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class VPXMonitoringService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(VPXMonitoringService.class);
  private final AtomicBoolean running = new AtomicBoolean(false);

  private Thread monitorThread;

  @Autowired
  private GameStatusService gameStatusService;

  @Autowired
  private GameService gameService;

  @Autowired
  private PopperService popperService;

  public void stopMonitoring() {
    if (monitorThread != null) {
      this.running.set(false);
    }
  }

  public void startMonitor() {
    this.running.set(true);
    monitorThread = new Thread(() -> {
      try {
        Thread.currentThread().setName("VPX Monitor Thread");
        LOG.info("VPX monitor started.");
        while (running.get()) {
          String tableName = getVPXTableName();
          if (tableName != null) {
            notifyTableStart(tableName);
          }
          else {
            notifyTableEnd();
          }
          Thread.sleep(3000);
        }
      } catch (Exception e) {
        LOG.info("VPX monitor failed: " + e.getMessage(), e);
      } finally {
        LOG.info(Thread.currentThread().getName() + " terminated.");
      }
    });
    monitorThread.start();
  }

  private void notifyTableEnd() {
    if (gameStatusService.getStatus().isActive()) {
      int gameId = gameStatusService.getStatus().getGameId();
      Game game = gameService.getGame(gameId);
      LOG.info(this.getClass().getSimpleName() + " notifying table end event of \"" + game.getGameDisplayName() + "\"");
      popperService.notifyTableStatusChange(game, false);
    }
  }

  private void notifyTableStart(@NonNull String tableName) {
    if (!gameStatusService.getStatus().isActive()) {
      Game game = gameService.getGameByName(tableName);
      if (game != null) {
        LOG.info(this.getClass().getSimpleName() + " notifying table start event of \"" + game.getGameDisplayName() + "\"");
        popperService.notifyTableStatusChange(game, true);
      }
      else {
        LOG.info(this.getClass().getSimpleName() + " registered a VPX window, but the game could not be resolved for name \"" + tableName + "\"");
      }
    }
  }

  private String getVPXTableName() {
    List<String> allWindowNames = SystemUtil.getAllWindowNames();
    for (String name : allWindowNames) {
      if (name.contains("VPinball".toLowerCase())) {
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
      startMonitor();
    }
  }
}
