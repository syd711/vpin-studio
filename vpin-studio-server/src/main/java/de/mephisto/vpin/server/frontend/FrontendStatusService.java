package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.FrontendControl;
import de.mephisto.vpin.restclient.frontend.FrontendControls;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.highscores.logging.HighscoreEventLog;
import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FrontendStatusService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(FrontendStatusService.class);

  private final List<TableStatusChangeListener> tableStatusChangeListeners = new ArrayList<>();
  private final List<FrontendStatusChangeListener> frontendStatusChangeListeners = new ArrayList<>();

  @Autowired
  private GameService gameService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameStatusService gameStatusService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  private boolean eventsEnabled = true;

  public FrontendControl getPinUPControlFor(VPinScreen screen) {
    return frontendService.getPinUPControlFor(screen);
  }

  public FrontendControls getPinUPControls() {
    return frontendService.getControls();
  }

  public void addTableStatusChangeListener(TableStatusChangeListener listener) {
    this.tableStatusChangeListeners.add(listener);
    this.tableStatusChangeListeners.sort((o1, o2) -> o2.getPriority() - o1.getPriority());
  }

  public void addFrontendStatusChangeListener(FrontendStatusChangeListener listener) {
    this.frontendStatusChangeListeners.add(listener);
  }

  public GameStatus getGameStatus() {
    return gameStatusService.getStatus();
  }

  public void notifyTableStatusChange(final Game game, final boolean started, TableStatusChangedOrigin origin) {
    if (!eventsEnabled) {
      LOG.info("Skipping table status change event, because the event handling is disabled");
      return;
    }

    TableStatusChangedEvent event = new TableStatusChangedEvent() {
      @NonNull
      @Override
      public Game getGame() {
        return game;
      }

      @Override
      public TableStatusChangedOrigin getOrigin() {
        return origin;
      }
    };

    for (TableStatusChangeListener listener : this.tableStatusChangeListeners) {
      if (started) {
        listener.tableLaunched(event);
      }
      else {
        listener.tableExited(event);
      }
    }
  }

  public boolean terminate() {
    boolean b = frontendService.killFrontend();
    for (FrontendStatusChangeListener listener : frontendStatusChangeListeners) {
      listener.frontendExited();
    }
    return b;
  }

  public void notifyFrontendLaunch() {
    if (!eventsEnabled) {
      LOG.info("Skipping notifyFrontendLaunch, because the event handling is disabled");
      return;
    }

    HighscoreEventLog highscoreEventLog = SLOG.finalizeEventLog();
    if (highscoreEventLog != null) {
      gameService.saveEventLog(highscoreEventLog);
    }

    for (FrontendStatusChangeListener listener : frontendStatusChangeListeners) {
      listener.frontendLaunched();
    }
  }

  public void notifyFrontendRestart() {
    if (!eventsEnabled) {
      LOG.info("Skipping notifyFrontendRestart, because the event handling is disabled");
      return;
    }

    for (FrontendStatusChangeListener listener : frontendStatusChangeListeners) {
      listener.frontendRestarted();
    }
  }

  public void notifyFrontendExit() {
    if (!eventsEnabled) {
      LOG.info("Skipping notifyFrontendExit, because the event handling is disabled");
      return;
    }

    //reset game status
    GameStatus status = gameStatusService.getStatus();
    if (status != null && status.isActive()) {
      gameStatusService.frontendExited();
    }

    HighscoreEventLog highscoreEventLog = SLOG.finalizeEventLog();
    if (highscoreEventLog != null) {
      gameService.saveEventLog(highscoreEventLog);
    }

    for (FrontendStatusChangeListener listener : frontendStatusChangeListeners) {
      listener.frontendExited();
    }
  }

  public boolean gameLaunch(@NonNull String table, @Nullable String emuDirOrName) {
    if (!eventsEnabled) {
      LOG.info("Skipping gameLaunch, because the event handling is disabled");
      return true;
    }

    LOG.info("Received game launch event for \"{}\", emu: \"{}\"", table, emuDirOrName);
    Game game = gameService.getGameByTableAndEmuParameter(table, emuDirOrName);
    if (game == null) {
      LOG.warn("No game found for name \"{}\" and emulator \"{}\"", table, emuDirOrName);
      return false;
    }

    if (gameStatusService.getStatus().getGameId() == game.getId()) {
      LOG.info("Skipped launch event, since the game has been marked as active already.");
      return false;
    }

    new Thread(() -> {
      Thread.currentThread().setName("Game Launch Thread");
      notifyTableStatusChange(game, true, TableStatusChangedOrigin.ORIGIN_POPPER);
    }).start();
    return true;
  }

  public boolean gameExit(@NonNull String table, @Nullable String emuDirOrName) {
    if (!eventsEnabled) {
      LOG.info("Skipping gameExit, because the event handling is disabled");
      return true;
    }

    LOG.info("Received game exit event for \"{}\", emu: \"{}\"", table, emuDirOrName);
    Game game = gameService.getGameByTableAndEmuParameter(table, emuDirOrName);
    if (game == null) {
      LOG.warn("No game found for name \"{}\" and emulator {}", table, emuDirOrName);
      return false;
    }

    return onGameExit(game);
  }

  private boolean onGameExit(@NonNull Game game) {
    new Thread(() -> {
      Thread.currentThread().setName("Game Exit Thread [" + game.getGameDisplayName() + "]");
      SLOG.initLog(game.getId());
      if (!gameStatusService.getStatus().isActive()) {
        LOG.info("Skipped exit event, since t11he no game is currently running.");
        SLOG.info("Skipped event processing, since the no game is currently running.");
        return;
      }

      notifyTableStatusChange(game, false, TableStatusChangedOrigin.ORIGIN_POPPER);
      HighscoreEventLog highscoreEventLog = SLOG.finalizeEventLog();
      if (highscoreEventLog != null) {
        gameService.saveEventLog(highscoreEventLog);
      }
    }).start();
    return true;
  }

  public void augmentWheel(Game game, String badge) {
    FrontendMediaItem frontendMediaItem = frontendService.getGameMedia(game).getDefaultMediaItem(VPinScreen.Wheel);
    if (frontendMediaItem != null) {
      File wheelIcon = frontendMediaItem.getFile();
      WheelAugmenter augmenter = new WheelAugmenter(wheelIcon);

      File badgeFile = systemService.getBadgeFile(badge);
      if (badgeFile.exists()) {
        augmenter.augment(badgeFile);
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.FRONTEND_MEDIA, null);
      }
    }
  }

  public void deAugmentWheel(Game game) {
    FrontendMediaItem frontendMediaItem = frontendService.getGameMedia(game).getDefaultMediaItem(VPinScreen.Wheel);
    if (frontendMediaItem != null) {
      File wheelIcon = frontendMediaItem.getFile();
      new WheelAugmenter(wheelIcon).deAugment();
      new WheelIconDelete(wheelIcon).delete();
      gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.FRONTEND_MEDIA, null);
    }
  }

  public boolean restart() {
    frontendService.restartFrontend();
    return true;
  }

  public JsonSettings getSettings() {
    return frontendService.getSettings();
  }

  public boolean saveSettings(Map<String, Object> options) {
    frontendService.saveSettings(options);
    return true;
  }

  @NonNull
  public List<GameEmulator> getBackglassGameEmulators() {
    return emulatorService.getBackglassGameEmulators();
  }

  @Nullable
  public GameEmulator getGameEmulator(int id) {
    return emulatorService.getGameEmulator(id);
  }

  public int getVersion() {
    return frontendService.getVersion();
  }

  public boolean isEventsEnabled() {
    return eventsEnabled;
  }

  public void setEventsEnabled(boolean eventsEnabled) {
    this.eventsEnabled = eventsEnabled;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    Thread shutdownHook = new Thread(this::notifyFrontendExit);
    Runtime.getRuntime().addShutdownHook(shutdownHook);

    frontendService.setFrontendStatusService(this);
    gameStatusService.init(this);
    LOG.info("{} initialization finished, running frontend version {}", this.getClass().getSimpleName(), frontendService.getVersion());
  }
}
