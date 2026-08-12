package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.commons.utils.FolderMonitoringThread;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPRegFile;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import jakarta.annotation.PreDestroy;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static de.mephisto.vpin.server.VPinStudioServer.Features;

/**
 * Watches the nvram/User/fpRAM folders of all VPX and Future Pinball emulators for highscore related
 * file changes. Whenever a relevant file is created or modified, the game matching the file's ROM name
 * or highscore/table filename is resolved and a highscore scan is triggered for it.
 */
@Service
public class HighscoreMonitoringService implements InitializingBean, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(HighscoreMonitoringService.class);

  private final static String VPREG_FILE_NAME = "VPReg.stg";
  private final static String HIGHSCORE_DEBOUNCE_KEY = "vpx-highscore-file-change";
  private final static int HIGHSCORE_DEBOUNCE_MS = 1000;

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> new Thread(r, "Highscore Monitor"));
  private volatile boolean emulatorWasRunning = false;

  @Autowired
  private GameService gameService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private VPinMameService vPinMameService;

  @Autowired
  private HighscoreService highscoreService;

  private final Debouncer debouncer = new Debouncer();

  /**
   * One folder-watcher per unique, existing folder (nvram/User/fpRAM), de-duplicated across emulators
   * that happen to share the same installation/nvram folder.
   */
  private final List<FolderMonitoringThread> folderMonitors = new CopyOnWriteArrayList<>();

  /**
   * Files reported as changed since the last debounce flush, together with the folder watch that
   * detected them (which tells us how to resolve the game for that file).
   */
  private final Map<File, FolderWatch> pendingFileChanges = new ConcurrentHashMap<>();

  private enum FileKind {
    NVRAM, VPX_USER, FP_RAM
  }

  /**
   * A monitored folder, the kind of highscore files expected in it, and all emulators that share it.
   */
  private static class FolderWatch {
    private final FileKind kind;
    private final List<GameEmulator> emulators = new CopyOnWriteArrayList<>();

    FolderWatch(@NonNull FileKind kind, @NonNull GameEmulator emulator) {
      this.kind = kind;
      this.emulators.add(emulator);
    }
  }

  //--------------------------------------------------------------- folder/file monitoring

  private void startFolderMonitors() {
    stopFolderMonitors();

    Map<String, FolderWatch> watchesByPath = new LinkedHashMap<>();
    for (GameEmulator emulator : emulatorService.getVpxGameEmulators()) {
      registerWatch(watchesByPath, resolveNvRamFolder(emulator), FileKind.NVRAM, emulator);
      registerWatch(watchesByPath, resolveUserFolder(emulator), FileKind.VPX_USER, emulator);
    }

    for (GameEmulator emulator : emulatorService.getFpGameEmulators()) {
      registerWatch(watchesByPath, resolveFpRamFolder(emulator), FileKind.FP_RAM, emulator);
    }

    for (Map.Entry<String, FolderWatch> entry : watchesByPath.entrySet()) {
      startFolderMonitor(new File(entry.getKey()), entry.getValue());
    }
  }

  private void stopFolderMonitors() {
    for (FolderMonitoringThread monitor : folderMonitors) {
      monitor.stopMonitoring();
    }
    folderMonitors.clear();
    pendingFileChanges.clear();
  }

  private void registerWatch(@NonNull Map<String, FolderWatch> watchesByPath, @Nullable File folder,
                              @NonNull FileKind kind, @NonNull GameEmulator emulator) {
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

    FolderWatch watch = watchesByPath.get(canonicalPath);
    if (watch == null) {
      watchesByPath.put(canonicalPath, new FolderWatch(kind, emulator));
    }
    else if (!watch.emulators.contains(emulator)) {
      //multiple emulators sharing the same installation/nvram folder
      watch.emulators.add(emulator);
    }
  }

  private void startFolderMonitor(@NonNull File folder, @NonNull FolderWatch watch) {
    FolderMonitoringThread monitor = new FolderMonitoringThread((changedFolder, file) -> {
      if (file != null && isRelevantFile(watch.kind, file)) {
        pendingFileChanges.put(file, watch);
        debouncer.debounce(HIGHSCORE_DEBOUNCE_KEY, this::flushPendingFileChanges, HIGHSCORE_DEBOUNCE_MS);
      }
    }, true, false);
    monitor.setFolder(folder);
    monitor.startMonitoring();
    folderMonitors.add(monitor);
    LOG.info("Monitoring \"{}\" for highscore file changes.", folder.getAbsolutePath());
  }

  private void flushPendingFileChanges() {
    if (SystemService.isPinballEmulatorRunning()) {
      //a table is still being played, wait for the emulator to exit before scanning its highscore file
      LOG.info("Deferring highscore scan of {} pending file change(s), a pinball emulator is still running.", pendingFileChanges.size());
      return;
    }

    Map<File, FolderWatch> changes = new LinkedHashMap<>(pendingFileChanges);
    pendingFileChanges.clear();

    for (Map.Entry<File, FolderWatch> entry : changes.entrySet()) {
      onHighscoreFileChanged(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Polled periodically so that file changes which arrived while a table was still being played get
   * flushed as soon as the emulator exits, instead of waiting for another file write to trigger it.
   */
  private void checkEmulatorState() {
    boolean emulatorRunning = SystemService.isPinballEmulatorRunning();
    if (emulatorWasRunning && !emulatorRunning) {
      flushPendingFileChanges();
    }
    emulatorWasRunning = emulatorRunning;
  }

  private void onHighscoreFileChanged(@NonNull File file, @NonNull FolderWatch watch) {
    List<Game> games = resolveGames(file, watch);
    if (games.isEmpty()) {
      LOG.info("Detected highscore file change of \"{}\", but no matching game was found.", file.getAbsolutePath());
      return;
    }

    for (Game game : games) {
      LOG.info("Detected highscore file change of \"{}\", triggering highscore scan for \"{}\".", file.getAbsolutePath(), game.getGameDisplayName());
      highscoreService.scanScore(game, EventOrigin.TABLE_EXIT_EVENT);
    }
  }

  //--------------------------------------------------------------- game resolution

  @NonNull
  private List<Game> resolveGames(@NonNull File file, @NonNull FolderWatch watch) {
    switch (watch.kind) {
      case NVRAM:
        return resolveGamesByRomOrTableName(watch.emulators, extractNvRamRomName(file));
      case VPX_USER:
        return resolveVpxUserGames(watch.emulators, file);
      case FP_RAM:
        return resolveFpRamGames(watch.emulators, file);
      default:
        return Collections.emptyList();
    }
  }

  @NonNull
  private List<Game> resolveVpxUserGames(@NonNull List<GameEmulator> emulators, @NonNull File file) {
    if (VPREG_FILE_NAME.equalsIgnoreCase(file.getName())) {
      return resolveVpRegGames(emulators, file);
    }
    return resolveVpxHighscoreTextGames(emulators, file);
  }

  /**
   * Text-based highscore files (e.g. for EM tables) live in the same folder as VPReg.stg. A changed
   * file is resolved by ROM or table name as usual, plus the game's highscore filename, since EM tables
   * are frequently identified by their highscore text filename rather than a ROM.
   */
  @NonNull
  private List<Game> resolveVpxHighscoreTextGames(@NonNull List<GameEmulator> emulators, @NonNull File file) {
    String baseName = FilenameUtils.getBaseName(file.getName());
    if (StringUtils.isEmpty(baseName)) {
      return Collections.emptyList();
    }

    List<Game> games = new ArrayList<>(resolveGamesByRomOrTableName(emulators, baseName));
    for (GameEmulator emulator : emulators) {
      for (Game game : gameService.getKnownGames(emulator.getId())) {
        if (!games.contains(game) && matchesEntries(List.of(file.getName()), game)) {
          games.add(game);
        }
      }
    }
    return games;
  }

  /**
   * VPReg.stg is a shared registry file for all tables of the emulator, so the changed entry cannot be
   * identified from the file event alone. Instead, every known game of the emulator is checked against
   * the registry's entries, matched by ROM, table name or highscore filename, and a scan is triggered
   * for each game that has a matching entry.
   */
  @NonNull
  private List<Game> resolveVpRegGames(@NonNull List<GameEmulator> emulators, @NonNull File file) {
    List<String> entries = new VPRegFile(file, null, null).getEntries();
    if (entries.isEmpty()) {
      return Collections.emptyList();
    }

    List<Game> games = new ArrayList<>();
    for (GameEmulator emulator : emulators) {
      for (Game game : gameService.getKnownGames(emulator.getId())) {
        if (matchesEntries(entries, game)) {
          games.add(game);
        }
      }
    }
    return games;
  }

  private static boolean matchesEntries(@NonNull List<String> entries, @NonNull Game game) {
    String rom = game.getRom();
    for (String entry : entries) {
      if (equalsCandidate(entry, game.getRom())
          || equalsCandidate(entry, game.getTableName())
          || equalsCandidate(entry, game.getScannedRom())
          || equalsCandidate(entry, game.getScannedAltRom())
          || equalsCandidate(entry, game.getHsFileName())
          || equalsCandidate(entry, game.getScannedHsFileName())
          || (!StringUtils.isEmpty(rom) && entry.equalsIgnoreCase(rom + "_VPX"))) {
        return true;
      }
    }
    return false;
  }

  private static boolean equalsCandidate(@NonNull String entry, @Nullable String candidate) {
    return !StringUtils.isEmpty(candidate) && (entry.equalsIgnoreCase(FilenameUtils.getBaseName(candidate))
      || entry.equalsIgnoreCase(candidate));
  }

  @NonNull
  private List<Game> resolveFpRamGames(@NonNull List<GameEmulator> emulators, @NonNull File file) {
    String baseName = FilenameUtils.getBaseName(file.getName());
    List<Game> games = new ArrayList<>();
    for (GameEmulator emulator : emulators) {
      Game game = gameService.getGameByBaseFilename(emulator.getId(), baseName);
      if (game != null) {
        games.add(game);
      }
    }
    return games;
  }

  @NonNull
  private List<Game> resolveGamesByRomOrTableName(@NonNull List<GameEmulator> emulators, @Nullable String name) {
    if (StringUtils.isEmpty(name)) {
      return Collections.emptyList();
    }

    List<Game> games = new ArrayList<>();
    for (GameEmulator emulator : emulators) {
      games.addAll(gameService.getGamesByRom(emulator.getId(), name));
    }
    return games;
  }

  @NonNull
  private static String extractNvRamRomName(@NonNull File file) {
    String baseName = FilenameUtils.getBaseName(file.getName());
    int nvOffsetIndex = baseName.indexOf(' ');
    return nvOffsetIndex > 0 ? baseName.substring(0, nvOffsetIndex) : baseName;
  }

  //--------------------------------------------------------------- folder/file resolution

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

  private static boolean isRelevantFile(@NonNull FileKind kind, @NonNull File file) {
    switch (kind) {
      case NVRAM:
        return isNvRamFile(file);
      case VPX_USER:
        return isVpxUserHighscoreFile(file);
      case FP_RAM:
        return isFpRamFile(file);
      default:
        return false;
    }
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
          startFolderMonitors();
          LOG.info("Enabled Highscore Monitor");
        }
        else {
          stopFolderMonitors();
          LOG.info("Disabled Highscore Monitor");
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to update highscore monitoring: {}", e.getMessage(), e);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (Features.HIGHSCORE_MONITORING) {
      preferencesService.addChangeListener(this);
      preferenceChanged(PreferenceNames.SERVER_SETTINGS, null, null);
      scheduler.scheduleAtFixedRate(this::checkEmulatorState, 5, 5, TimeUnit.SECONDS);
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }

  @PreDestroy
  public void shutdown() {
    scheduler.shutdownNow();
    stopFolderMonitors();
    debouncer.shutdown();
    LOG.info("Folder monitoring has been shut down.");
  }
}
