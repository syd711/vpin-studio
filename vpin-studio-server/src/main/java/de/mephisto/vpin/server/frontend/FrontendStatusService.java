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
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.LongSupplier;

@Service
public class FrontendStatusService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(FrontendStatusService.class);

  /**
   * Polling interval of the emulator process check for tables launched from the Studio.
   */
  final static int STUDIO_LAUNCH_POLL_INTERVAL_SECONDS = 3;
  /**
   * Maximum time to wait for the emulator to show up after a Studio launch (Steam games may take a while).
   */
  final static long STUDIO_LAUNCH_STARTUP_TIMEOUT_MS = 90_000;
  /**
   * Number of consecutive negative checks before the table is considered as exited,
   * e.g. to survive the FPLoader → Future Pinball handover or a recreated VPX player window.
   */
  final static int STUDIO_LAUNCH_REQUIRED_MISSES = 2;

  //non-final and package-private so that tests can replace the process check, the clock and the scheduler
  BooleanSupplier emulatorRunningCheck = SystemService::isPinballEmulatorRunning;
  LongSupplier clock = System::currentTimeMillis;
  ScheduledExecutorService studioLaunchScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
    Thread thread = new Thread(r, "Studio Launch Monitor");
    thread.setDaemon(true);
    return thread;
  });
  private ScheduledFuture<?> studioLaunchMonitor;
  private StudioLaunchMonitor activeStudioLaunchMonitor;

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
      private final Date eventTime = new Date();

      @NonNull
      @Override
      public Game getGame() {
        return game;
      }

      @Override
      public TableStatusChangedOrigin getOrigin() {
        return origin;
      }

      @Override
      public long getEventAgeMs() {
        return System.currentTimeMillis() - eventTime.getTime();
      }
    };

    for (TableStatusChangeListener listener : this.tableStatusChangeListeners) {
      try {
        if (started) {
          listener.tableLaunched(event);
        }
        else {
          listener.tableExited(event);
        }
      }
      catch (Exception e) {
        // A single misbehaving listener must not stop the remaining listeners from being notified,
        // e.g. InputEventService (which tracks emulatorRunning for the pause button) registers last
        // and would otherwise silently stop receiving table launch/exit events until a restart.
        LOG.error("Listener {} failed to handle table {} event for \"{}\": {}",
            listener.getClass().getSimpleName(), started ? "launch" : "exit", game.getGameDisplayName(), e.getMessage(), e);
      }
    }
  }

  /**
   * Notifies all listeners synchronously that the given table is about to be launched.
   */
  public void notifyTableLaunching(@NonNull Game game) {
    if (!eventsEnabled) {
      LOG.info("Skipping table launching event, because the event handling is disabled");
      return;
    }

    for (TableStatusChangeListener listener : this.tableStatusChangeListeners) {
      try {
        listener.tableLaunching(game);
      }
      catch (Exception e) {
        LOG.error("Listener {} failed to prepare the launch of \"{}\": {}",
            listener.getClass().getSimpleName(), game.getGameDisplayName(), e.getMessage(), e);
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

    //the frontend has taken over again
    stopStudioLaunchMonitor();

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

    //the frontend has taken over again
    stopStudioLaunchMonitor();

    //runs before the frontend receives the response and starts the emulator
    notifyTableLaunching(game);

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

    return onGameExit(game, TableStatusChangedOrigin.ORIGIN_POPPER);
  }

  /**
   * Fires the table launch event for a table that has been launched from the Studio and starts monitoring
   * the emulator processes, so that the table exit event is fired once no emulator is running anymore.
   * The frontend is not involved in this case, so it won't send any launch or exit events.
   */
  public void notifyStudioLaunch(@NonNull Game game) {
    if (!eventsEnabled) {
      LOG.info("Skipping Studio launch event, because the event handling is disabled");
      return;
    }

    LOG.info("Received Studio launch event for \"{}\"", game.getGameDisplayName());
    startStudioLaunchMonitor(game);

    new Thread(() -> {
      Thread.currentThread().setName("Studio Game Launch Thread");
      notifyTableStatusChange(game, true, TableStatusChangedOrigin.ORIGIN_STUDIO);
    }).start();
  }

  private synchronized void startStudioLaunchMonitor(@NonNull Game game) {
    stopStudioLaunchMonitor();
    StudioLaunchMonitor monitor = new StudioLaunchMonitor(game);
    activeStudioLaunchMonitor = monitor;
    studioLaunchMonitor = studioLaunchScheduler.scheduleWithFixedDelay(monitor, STUDIO_LAUNCH_POLL_INTERVAL_SECONDS, STUDIO_LAUNCH_POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);
  }

  /**
   * The monitor of the current Studio launch, if any. Visible for testing.
   */
  @Nullable
  synchronized Runnable getActiveStudioLaunchMonitor() {
    return activeStudioLaunchMonitor;
  }

  private synchronized void stopStudioLaunchMonitor() {
    if (studioLaunchMonitor != null) {
      studioLaunchMonitor.cancel(false);
      studioLaunchMonitor = null;
      activeStudioLaunchMonitor = null;
      LOG.info("Stopped emulator monitoring of the last Studio launch.");
    }
  }

  private synchronized void finishStudioLaunchMonitor(@NonNull StudioLaunchMonitor monitor) {
    //the monitor may have been replaced by a newer launch in the meantime
    if (activeStudioLaunchMonitor == monitor) {
      studioLaunchMonitor.cancel(false);
      studioLaunchMonitor = null;
      activeStudioLaunchMonitor = null;
      onGameExit(monitor.game, TableStatusChangedOrigin.ORIGIN_STUDIO);
    }
  }

  /**
   * Polls the emulator processes: first waits for the emulator to be started, then waits for it to be gone.
   */
  private class StudioLaunchMonitor implements Runnable {
    private final Game game;
    private final long startedAt = clock.getAsLong();
    private boolean emulatorDetected = false;
    private int misses = 0;

    StudioLaunchMonitor(@NonNull Game game) {
      this.game = game;
    }

    @Override
    public void run() {
      try {
        boolean running = emulatorRunningCheck.getAsBoolean();
        if (!emulatorDetected) {
          if (running) {
            emulatorDetected = true;
            LOG.info("Emulator for Studio launch of \"{}\" detected, waiting for its exit.", game.getGameDisplayName());
          }
          else if (clock.getAsLong() - startedAt > STUDIO_LAUNCH_STARTUP_TIMEOUT_MS) {
            LOG.warn("No emulator detected {}ms after the Studio launch of \"{}\", firing table exit.", STUDIO_LAUNCH_STARTUP_TIMEOUT_MS, game.getGameDisplayName());
            finishStudioLaunchMonitor(this);
          }
          return;
        }

        if (running) {
          misses = 0;
          return;
        }

        misses++;
        if (misses >= STUDIO_LAUNCH_REQUIRED_MISSES) {
          LOG.info("No emulator running anymore, Studio launched table \"{}\" has been exited.", game.getGameDisplayName());
          finishStudioLaunchMonitor(this);
        }
      }
      catch (Exception e) {
        //an exception would cancel the scheduled task silently
        LOG.error("Studio launch monitoring failed for \"{}\": {}", game.getGameDisplayName(), e.getMessage(), e);
      }
    }
  }

  private boolean onGameExit(@NonNull Game game, @NonNull TableStatusChangedOrigin origin) {
    new Thread(() -> {
      Thread.currentThread().setName("Game Exit Thread [" + game.getGameDisplayName() + "]");
      SLOG.initLog(game.getId());
      if (!gameStatusService.getStatus().isActive()) {
        LOG.info("Skipped exit event, since no game is currently running.");
        SLOG.info("Skipped event processing, since the no game is currently running.");
        return;
      }

      notifyTableStatusChange(game, false, origin);
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
        boolean rotate = true;
        Optional<MonitorInfo> first = systemService.getMonitorInfos().stream().filter(MonitorInfo::isPrimary).findFirst();
        if (first.isPresent()) {
          rotate = !first.get().isPortraitMode();
        }
          if ("apng".equalsIgnoreCase(FilenameUtils.getExtension(wheelIcon.getName()))){
              augmenter.augmentApng(badgeFile, rotate);
          } else {
              augmenter.augment(badgeFile, rotate);
          }
        gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.FRONTEND_MEDIA, null);
      }
    }
  }

  public boolean isWheelAugmented(Game game) {
    FrontendMediaItem frontendMediaItem = frontendService.getGameMedia(game).getDefaultMediaItem(VPinScreen.Wheel);
    if (frontendMediaItem != null) {
      File wheelIcon = frontendMediaItem.getFile();
      return new WheelAugmenter(wheelIcon).isAugmented();
    }
    return false;
  }

  public void deAugmentWheel(Game game) {
    FrontendMediaItem frontendMediaItem = frontendService.getGameMedia(game).getDefaultMediaItem(VPinScreen.Wheel);
    if (frontendMediaItem != null) {
      File wheelIcon = frontendMediaItem.getFile();
      WheelAugmenter augmenter = new WheelAugmenter(wheelIcon);
      if (augmenter.isAugmented()) {
        augmenter.deAugment();
        new WheelIconDelete(wheelIcon).delete();
      }

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
