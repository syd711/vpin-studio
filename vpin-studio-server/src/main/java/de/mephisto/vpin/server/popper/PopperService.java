package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PopperService {
  private final static Logger LOG = LoggerFactory.getLogger(PopperService.class);

  private final List<TableStatusChangeListener> listeners = new ArrayList<>();

  @Autowired
  private HighscoreService highscoreService;

  public void notifyTableStatusChange(final Game game, final boolean started) {
    new Thread(() -> {
      if (started) {
        this.executeTableLaunchCommands(game);
      }
      else {
        this.executeTableExitCommands(game);
      }

      TableStatusChangedEvent event = () -> game;
      for (TableStatusChangeListener listener : this.listeners) {
        if (started) {
          listener.tableLaunched(event);
        }
        else {
          listener.tableExited(event);
        }
      }
    }).start();
  }

  @SuppressWarnings("unused")
  public void addTableStatusChangeListener(TableStatusChangeListener listener) {
    this.listeners.add(listener);
  }

  @SuppressWarnings("unused")
  public void removeTableStatusChangeListener(TableStatusChangeListener listener) {
    this.listeners.remove(listener);
  }


  public void executeTableLaunchCommands(Game game) {
    LOG.info("Executing table launch commands for '" + game + "'");
  }

  public void executeTableExitCommands(Game game) {
    LOG.info("Executing table exit commands for '" + game + "'");
    highscoreService.updateHighscore(game);
  }
}
