package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.server.frontend.FrontendStatusChangeListener;
import de.mephisto.vpin.server.highscores.monitoring.HighscoreMonitoringService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.TableStatusChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;

@Service
public class GameStatusService implements TableStatusChangeListener, FrontendStatusChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(GameStatusService.class);

  @Autowired
  private HighscoreMonitoringService highscoreMonitoringService;

  private final GameStatus status = new GameStatus();

  // true when VPXMonitor detect a VPX window and cannot recognize the Game 
  private boolean forceActive;

  public boolean isActive() {
    return status.isActive() || forceActive;
  }
  public void setForceActive(boolean active) {
    this.forceActive = active;
  }

  public GameStatus getStatus() {
    return status;
  }

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    status.setStarted(new Date());
    status.setGameId(event.getGame().getId());
    LOG.info("GameStatusService saved \"" + event.getGame().getGameDisplayName() + "\" as active game.");
    highscoreMonitoringService.startMonitoring(event.getGame());
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    resetStatus();
    highscoreMonitoringService.stopMonitoring();
  }

  @Override
  public void frontendLaunched() {
    status.setGameId(-1);
  }

  @Override
  public void frontendExited() {
    resetStatus();
    highscoreMonitoringService.stopMonitoring();
  }

  @Override
  public void frontendRestarted() {
    resetStatus();
    highscoreMonitoringService.stopMonitoring();
  }

  public void init(FrontendStatusService frontendStatusService) {
    frontendStatusService.addTableStatusChangeListener(this);
    frontendStatusService.addFrontendStatusChangeListener(this);
  }

  public void resetStatus() {
    status.setGameId(-1);
    SLOG.info("Resetted active game status");
  }
}
