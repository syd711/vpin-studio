package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.server.alx.AlxService;
import de.mephisto.vpin.server.frontend.FrontendStatusChangeListener;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.TableStatusChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class GameStatusService implements TableStatusChangeListener, FrontendStatusChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(GameStatusService.class);

  private final GameStatus status = new GameStatus();

  // true when VPXMonitor detect a VPX window and cannot recognize the Game 
  private boolean forceActive;

  public boolean isActive() {
    return status.isActive() || forceActive;
  }

  public void setForceActive(boolean active) {
    this.forceActive = active;
  }

  @Autowired
  private AlxService alxService;


  public GameStatus getStatus() {
    return status;
  }

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    status.setStarted(new Date());
    status.setGameId(event.getGame().getId());
    LOG.info("GameStatusService saved \"" + event.getGame().getGameDisplayName() + "\" as active game.");
  }

  public void setActiveStatus(int id) {
    status.setGameId(id);
    status.setStarted(new Date());
    LOG.info("GameStatusService saved game id \"" + id + "\" as active game.");
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    resetStatus();
  }

  @Override
  public void frontendLaunched() {
    status.setGameId(-1);
  }

  @Override
  public void frontendExited() {
    resetStatus();
  }

  @Override
  public void frontendRestarted() {
    resetStatus();
  }

  public void init(FrontendStatusService frontendStatusService) {
    frontendStatusService.addTableStatusChangeListener(this);
    frontendStatusService.addFrontendStatusChangeListener(this);
  }

  public void resetStatus() {
    status.finishPause();
    if (status.getGameId() != -1) {
      long pauseDurationMs = getStatus().getPauseDurationMs();
      alxService.substractPlayTimeForGame(status.getGameId(), pauseDurationMs);
    }
    status.setGameId(-1);
    SLOG.info("Resetted active game status");
  }

}
