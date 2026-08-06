package de.mephisto.vpin.server.highscores;

import com.sun.jna.platform.DesktopWindow;
import com.sun.jna.platform.WindowUtils;
import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.FolderMonitoringThread;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import jakarta.annotation.PreDestroy;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

@Service
public class HighscoreMonitoringService implements InitializingBean, PreferenceChangedListener, Runnable {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreMonitoringService.class);

  private final static String VPREG_FILE_NAME = "VPReg.stg";
  private final static String HIGHSCORE_DEBOUNCE_KEY = "vpx-highscore-file-change";
  private final static int HIGHSCORE_DEBOUNCE_MS = 1000;

  private final AtomicBoolean running = new AtomicBoolean(false);

  @Autowired
  private GameStatusService gameStatusService;

  @Autowired
  private GameService gameService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private VPinMameService vPinMameService;

  @Autowired
  private HighscoreService highscoreService;

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final Debouncer debouncer = new Debouncer();

  /**
   * One folder-watcher per unique, existing folder (nvram/User/fpRAM), de-duplicated across emulators
   * that happen to share the same installation/nvram folder.
   */
  private final List<FolderMonitoringThread> folderMonitors = new CopyOnWriteArrayList<>();

  /**
   * Set while a highscore-relevant file changes while a game is active. Flushed (i.e. the highscore
   * scan is finally triggered) once no pinball emulator process is running anymore, instead of firing
   * on every single file write while the table is being played.
   */
  private final AtomicInteger pendingHighscoreGameId = new AtomicInteger(-1);
  private volatile boolean emulatorWasRunning = false;

  @Override
  public void run() {
    try {
      if (!running.get()) {
        return;
      }
      Thread.currentThread().setName("Highscore Monitor Thread");

      boolean emulatorRunning = SystemService.isPinballEmulatorRunning();
      if (emulatorWasRunning && !emulatorRunning) {
        flushPendingHighscoreChange();
      }
      emulatorWasRunning = emulatorRunning;

      List<GameEmulator> emulators = emulatorService.getValidGameEmulators();

      List<DesktopWindow> windows = WindowUtils.getAllWindows(true);
      boolean playerRunning = windows.stream().anyMatch(wdw -> Strings.CI.contains(wdw.getTitle(), "Visual Pinball Player"));

      if (playerRunning && !gameStatusService.isActive()) {
        int emuId = -1;
        String tableName = null;

        for (DesktopWindow wdw : windows) {
          for (GameEmulator emu : emulators) {
            if (Strings.CI.startsWith(wdw.getFilePath(), emu.getInstallationDirectory())) {
              String windowTitle = wdw.getTitle();
              //LOG.info("VPX process detected with window title " + wdw.getTitle());
              if (windowTitle.contains("[") && windowTitle.contains("]")) {
                emuId = emu.getId();
                tableName = windowTitle.substring(windowTitle.indexOf("[") + 1, windowTitle.length() - 1);
              }
            }
          }
        }
        if (tableName != null) {
          notifyTableStartByFileName(emuId, tableName);
        }
      }
      else if (!playerRunning && gameStatusService.isActive()) {
        notifyTableEnd();
      }
    }
    catch (Exception e) {
      LOG.info("VPX Monitor Thread failed: {}", e.getMessage(), e);
    }
  }

  private void notifyTableEnd() {
    int gameId = gameStatusService.getStatus().getGameId();
    Game game = gameId > 0 ? gameService.getGame(gameId) : null;

    if (game != null) {
      LOG.info("{} notifying table end event of \"{}\"", this.getClass().getSimpleName(), game.getGameDisplayName());
      frontendStatusService.notifyTableStatusChange(game, false, TableStatusChangedOrigin.ORIGIN_POPPER);
    }
    else {
      LOG.info("{} unregistered a VPX window, but the game could not be resolved", this.getClass().getSimpleName());
      gameStatusService.setForceActive(false);
    }
  }

  private void notifyTableStartByFileName(int emuId, @NonNull String tableName) {
    LOG.info("Detected VPX running with table filename \"{}.vpx\", resolving game for it.", tableName);

    Game game = gameService.getGameByFilename(emuId, tableName + ".vpx");
    if (game == null) {
      game = gameService.getGameByFilename(emuId, tableName + ".vpt");
    }

    if (game != null) {
      LOG.info("{} notifying table start event of \"{}\"", this.getClass().getSimpleName(), game.getGameDisplayName());
      frontendStatusService.notifyTableStatusChange(game, true, TableStatusChangedOrigin.ORIGIN_POPPER);
    }
    else {
      LOG.info("{} registered a VPX window, but the game could not be resolved for name \"{}\"", this.getClass().getSimpleName(), tableName);
      gameStatusService.setForceActive(true);
    }
  }

  //--------------------------------------------------------------- folder/file monitoring

  private void startFolderMonitors() {
    stopFolderMonitors();

    Set<String> registeredPaths = new HashSet<>();
    for (GameEmulator emulator : emulatorService.getVpxGameEmulators()) {
      registerFolderMonitor(registeredPaths, resolveNvRamFolder(emulator), HighscoreMonitoringService::isNvRamFile);
      registerFolderMonitor(registeredPaths, resolveUserFolder(emulator), HighscoreMonitoringService::isVpxUserHighscoreFile);
    }

    for (GameEmulator emulator : emulatorService.getFpGameEmulators()) {
      registerFolderMonitor(registeredPaths, resolveFpRamFolder(emulator), HighscoreMonitoringService::isFpRamFile);
    }
  }

  private void stopFolderMonitors() {
    for (FolderMonitoringThread monitor : folderMonitors) {
      monitor.stopMonitoring();
    }
    folderMonitors.clear();
  }

  private void registerFolderMonitor(@NonNull Set<String> registeredPaths, @Nullable File folder, @NonNull Predicate<File> filter) {
    if (folder == null || !folder.isDirectory()) {
      return;
    }

    String canonicalPath;
    try {
      canonicalPath = folder.getCanonicalPath();
    }
    catch (IOException e) {
      canonicalPath = folder.getAbsolutePath();
    }

    if (!registeredPaths.add(canonicalPath)) {
      //already monitored, e.g. multiple emulators sharing the same installation/nvram folder
      return;
    }

    FolderMonitoringThread monitor = new FolderMonitoringThread((changedFolder, file) -> {
      if (file == null || filter.test(file)) {
        debouncer.debounce(HIGHSCORE_DEBOUNCE_KEY, this::onHighscoreFileChanged, HIGHSCORE_DEBOUNCE_MS);
      }
    }, true, false);
    monitor.setFolder(folder);
    monitor.startMonitoring();
    folderMonitors.add(monitor);
    LOG.info("Monitoring \"{}\" for highscore file changes.", canonicalPath);
  }

  private void onHighscoreFileChanged() {
    if (gameStatusService.isActive()) {
      int gameId = gameStatusService.getStatus().getGameId();
      if (gameId > 0) {
        pendingHighscoreGameId.set(gameId);
        LOG.info("Detected highscore related file change for active game id {}, deferring highscore scan until the emulator exits.", gameId);
      }
    }
  }

  private void flushPendingHighscoreChange() {
    int gameId = pendingHighscoreGameId.getAndSet(-1);
    if (gameId > 0) {
      Game game = gameService.getGame(gameId);
      if (game != null) {
        LOG.info("{} emulator process ended, firing deferred highscore scan for \"{}\".", this.getClass().getSimpleName(), game.getGameDisplayName());
        highscoreService.scanScore(game, EventOrigin.TABLE_EXIT_EVENT);
      }
    }
  }

  @Nullable
  private File resolveNvRamFolder(@NonNull GameEmulator emulator) {
    File folder = vPinMameService.getNvRamFolder();
    if (folder == null) {
      folder = new File(emulator.getMameFolder(), "nvram");
    }
    return folder;
  }

  @NonNull
  private File resolveUserFolder(@NonNull GameEmulator emulator) {
    return new File(emulator.getInstallationFolder(), "User");
  }

  @NonNull
  private File resolveFpRamFolder(@NonNull GameEmulator emulator) {
    return new File(emulator.getInstallationFolder(), "fpRAM");
  }

  private static boolean isNvRamFile(@NonNull File file) {
    return "nv".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()));
  }

  private static boolean isVpxUserHighscoreFile(@NonNull File file) {
    String name = file.getName();
    return VPREG_FILE_NAME.equalsIgnoreCase(name) || "txt".equalsIgnoreCase(FilenameUtils.getExtension(name));
  }

  private static boolean isFpRamFile(@NonNull File file) {
    return "fpram".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()));
  }

  //---------------------------------------------------------------

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    try {
      if (PreferenceNames.SERVER_SETTINGS.equalsIgnoreCase(propertyName)) {
        ServerSettings serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
        if (serverSettings.isHighscoreMonitorEnabled()) {
          running.set(true);
          startFolderMonitors();
          LOG.info("Enabled Highscore Monitor");
        }
        else {
          running.set(false);
          stopFolderMonitors();
          LOG.info("Disabled Highscore Monitor");
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to update VPX monitoring: {}", e.getMessage(), e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (Features.HIGHSCORE_MONITORING) {
      scheduler.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);
      preferencesService.addChangeListener(this);
      preferenceChanged(PreferenceNames.SERVER_SETTINGS, null, null);
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }

  @PreDestroy
  public void shutdown() {
    scheduler.shutdownNow();
    stopFolderMonitors();
    debouncer.shutdown();
    LOG.info("Folder monitoring scheduler has been shut down.");
  }
}
