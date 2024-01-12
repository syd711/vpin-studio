package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.popper.PopperStatusChangeListener;
import de.mephisto.vpin.server.popper.TableStatusChangedEvent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class GameStatusService implements PopperStatusChangeListener, InitializingBean {

  @Autowired
  private PopperService popperService;

  private GameStatus status = new GameStatus();

  public GameStatus getStatus() {
    return status;
  }

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    status.setStarted(new Date());
    status.setGameId(event.getGame().getId());
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    status.setGameId(event.getGame().getId());
  }

  @Override
  public void popperLaunched() {
    status.setGameId(-1);
  }

  @Override
  public void popperExited() {
    status.setGameId(-1);
  }

  @Override
  public void popperRestarted() {
    status.setGameId(-1);
  }

  @Override
  public void afterPropertiesSet() {
    popperService.addPopperStatusChangeListener(this);
  }
}
