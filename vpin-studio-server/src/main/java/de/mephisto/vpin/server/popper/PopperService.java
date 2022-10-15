package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.HighscoreService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PopperService {
  private final static Logger LOG = LoggerFactory.getLogger(PopperService.class);

  private final List<TableStatusChangeListener> listeners = new ArrayList<>();
  private final List<PopperLaunchListener> launchListeners = new ArrayList<>();

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

  public boolean isPinUPRunning() {
    Optional<ProcessHandle> pinUP = ProcessHandle.allProcesses().filter(p -> p.info().command().isPresent() && p.info().command().get().contains("PinUP")).findFirst();
    return pinUP.isPresent();
  }

  @SuppressWarnings("unused")
  public void addPopperLaunchListener(PopperLaunchListener listener) {
    this.launchListeners.add(listener);
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

  public void notifyPopperLaunch() {
    for (PopperLaunchListener launchListener : launchListeners) {
      launchListener.popperLaunched();
    }
  }
}
