package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.server.GameInfo;
import de.mephisto.vpin.server.highscores.HighscoreManager;
import de.mephisto.vpin.server.util.SqliteConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PopperManager {
  private final static Logger LOG = LoggerFactory.getLogger(PopperManager.class);

  private final List<TableStatusChangeListener> listeners = new ArrayList<>();

  @Autowired
  private SqliteConnector connector;

  @Autowired
  private HighscoreManager highscoreManager;

  public void notifyTableStatusChange(final GameInfo game, final boolean started) {
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


  public void executeTableLaunchCommands(GameInfo game) {
    LOG.info("Executing table launch commands for '" + game + "'");
  }

  public void executeTableExitCommands(GameInfo game) {
    LOG.info("Executing table exit commands for '" + game + "'");
    highscoreManager.invalidateHighscore(game);
  }

  public String validateScreenConfiguration(PopperScreen screen) {
    PinUPControl fn = null;
    switch (screen) {
      case Other2: {
        fn = connector.getFunction(PinUPControl.FUNCTION_SHOW_OTHER);
        break;
      }
      case GameHelp: {
        fn = connector.getFunction(PinUPControl.FUNCTION_SHOW_HELP);
        break;
      }
      case GameInfo: {
        fn = connector.getFunction(PinUPControl.FUNCTION_SHOW_FLYER);
        break;
      }
      default: {

      }
    }

    if (fn != null) {
      if (!fn.isActive()) {
        return "The screen has not been activated.";
      }

      if (fn.getCtrlKey() == 0) {
        return "The screen is not bound to any key.";
      }
    }

    return null;
  }
}
