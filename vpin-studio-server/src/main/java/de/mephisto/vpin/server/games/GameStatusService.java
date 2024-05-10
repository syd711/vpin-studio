package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.server.highscores.monitoring.HighscoreMonitoringService;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.popper.PopperStatusChangeListener;
import de.mephisto.vpin.server.popper.TableStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class GameStatusService implements PopperStatusChangeListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(GameStatusService.class);

  @Autowired
  private PopperService popperService;

  @Autowired
  private HighscoreMonitoringService highscoreMonitoringService;

  private final GameStatus status = new GameStatus();

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
    status.setGameId(event.getGame().getId());
    highscoreMonitoringService.stopMonitoring();
  }

  @Override
  public void popperLaunched() {
    status.setGameId(-1);
  }

  @Override
  public void popperExited() {
    status.setGameId(-1);
    highscoreMonitoringService.stopMonitoring();
  }

  @Override
  public void popperRestarted() {
    status.setGameId(-1);
    highscoreMonitoringService.stopMonitoring();
  }

  @Override
  public void afterPropertiesSet() {
    popperService.addPopperStatusChangeListener(this);
  }
}
