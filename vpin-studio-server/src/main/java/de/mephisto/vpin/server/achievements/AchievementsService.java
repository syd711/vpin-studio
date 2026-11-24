package de.mephisto.vpin.server.achievements;

import de.mephisto.vpin.commons.fx.achievements.BadgeMedal;
import de.mephisto.vpin.commons.fx.notifications.Notification;
import de.mephisto.vpin.commons.fx.notifications.NotificationFactory;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.achievements.AchievementsSettings;
import de.mephisto.vpin.restclient.achievements.BadgeInfo;
import de.mephisto.vpin.restclient.achievements.LevelInfo;
import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.server.achievements.model.AchievementPlayerState;
import de.mephisto.vpin.server.achievements.model.AchievementPlayerStateRepository;
import de.mephisto.vpin.server.achievements.model.AchievementSession;
import de.mephisto.vpin.server.achievements.model.AchievementSessionRepository;
import de.mephisto.vpin.server.achievements.model.AchievementUnlock;
import de.mephisto.vpin.server.achievements.model.AchievementUnlockRepository;
import de.mephisto.vpin.server.achievements.nvram.NvramSnapshot;
import de.mephisto.vpin.server.achievements.progression.BadgeEvaluator;
import de.mephisto.vpin.server.achievements.progression.BadgeStats;
import de.mephisto.vpin.server.achievements.progression.LevelCalculator;
import de.mephisto.vpin.server.achievements.progression.RarityTier;
import de.mephisto.vpin.server.achievements.rules.AchievementRule;
import de.mephisto.vpin.server.achievements.rules.PlayerProgress;
import de.mephisto.vpin.server.achievements.rules.RuleEvaluator;
import de.mephisto.vpin.server.achievements.scoreserver.LiveNvram;
import de.mephisto.vpin.server.achievements.scoreserver.ScoreServerClient;
import de.mephisto.vpin.server.achievements.scoreserver.ScoreServerGame;
import de.mephisto.vpin.server.achievements.scoreserver.VpxPluginInstaller;
import de.mephisto.vpin.restclient.achievements.VpxPluginStatus;
import de.mephisto.vpin.restclient.util.OSUtil;
import de.mephisto.vpin.server.alx.AlxService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.TableStatusChangeListener;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.TableStatusChangedEvent;
import de.mephisto.vpin.restclient.notifications.NotificationSettings;
import de.mephisto.vpin.server.notifications.NotificationService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.util.ServerMessages;
import de.mephisto.vpin.server.players.PlayerLifecycleListener;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Achievement tracking (port of the VPX Achievement Watcher): snapshots the audits of the .nv file when a table is
 * launched and evaluates the achievement rules for every tracked player when the table is exited.
 */
@Service
public class AchievementsService implements TableStatusChangeListener, PreferenceChangedListener, PlayerLifecycleListener, InitializingBean {
  private static final Logger LOG = LoggerFactory.getLogger(AchievementsService.class);

  private static final long NV_FLUSH_TIMEOUT_MS = 10_000;
  private static final long ALX_CACHE_MS = 5 * 60 * 1000;
  private static final double BADGE_NOTIFICATION_SIZE = 750; // size of the notification image view
  private static final String UI_BUNDLE = "de/mephisto/vpin/ui/messages/messages";

  @Autowired
  private PreferencesService preferencesService;
  @Autowired
  private FrontendStatusService frontendStatusService;
  @Autowired
  private FrontendService frontendService;
  @Autowired
  private PlayerService playerService;
  @Autowired
  private FolderLookupService folderLookupService;
  @Autowired
  private AlxService alxService;
  @Autowired
  private NotificationService notificationService;
  @Autowired
  private AchievementRuleStore ruleStore;
  @Autowired
  private AchievementStateStore stateStore;
  @Autowired
  private AchievementGameIndex gameIndex;
  @Autowired
  private AchievementRarityCache rarityCache;
  @Autowired
  private AchievementUnlockRepository unlockRepository;
  @Autowired
  private AchievementSessionRepository sessionRepository;
  @Autowired
  private AchievementPlayerStateRepository stateRepository;
  @Autowired
  private VpxPluginInstaller pluginInstaller;

  /** minimum time between two live checks of a game */
  private static final long LIVE_EVAL_MS = 2000;
  /** the live NVRAM is searched in the .nv file with the first snapshots only */
  private static final int MAX_ALIGN_ATTEMPTS = 20;

  private volatile ScoreServerClient scoreServer = new ScoreServerClient(new ScoreServerListener());
  /** the table played with live tracking, null if none */
  private volatile LiveTable live;

  private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "Achievements Evaluation"));
  private final Map<Integer, ActiveSession> activeSessions = new ConcurrentHashMap<>();
  private final List<AchievementsChangeListener> listeners = new CopyOnWriteArrayList<>();
  private AchievementsSettings settings = new AchievementsSettings();

  private volatile long alxPlaytimeSec = -1;
  private volatile long alxFetchedAt = 0;

  /**
   * Notified after achievements of players changed (used by the cloud sync).
   */
  public interface AchievementsChangeListener {
    void achievementsChanged(@NonNull Set<Long> playerIds);
  }

  record ActiveSession(int gameId, String rom, File nvFile, long nvModified, NvramSnapshot start, Instant startedAt) {
  }

  /**
   * Data of the score-server plugin for an evaluated game.
   *
   * @param playerCount  players of the pinball game(s), null if unknown
   * @param bestBall     best ball of player 1, null if unknown
   * @param playerScores player (1 based) -> final score, empty if unknown
   * @param liveTracked  the session is one pinball game reported by the plugin
   * @param liveUnlocks  playerId -> achievements unlocked during the game
   */
  public record GameExtras(@Nullable Integer playerCount, ScoreServerGame.@Nullable BestBall bestBall,
                           @NonNull Map<Integer, Long> playerScores, boolean liveTracked,
                           @NonNull Map<Long, List<AchievementUnlock>> liveUnlocks) {
    static GameExtras of(@NonNull ScoreServerGame game, @Nullable LiveRound round) {
      return new GameExtras(game.getPlayers(), game.getBestBall(1), game.getScores(), true,
          round != null ? round.liveUnlocks : Collections.emptyMap());
    }
  }

  /**
   * A table played while the score-server plugin is connected.
   */
  static class LiveTable {
    final Game game;
    final ActiveSession session;
    byte[] nvRaw;
    Integer offset;
    int alignAttempts;
    /** the plugin reports every pinball game (round_start), games are evaluated one by one */
    boolean roundsMode;
    LiveRound round;

    LiveTable(Game game, ActiveSession session) {
      this.game = game;
      this.session = session;
    }
  }

  /**
   * A running pinball game.
   */
  static class LiveRound {
    final ScoreServerGame game;
    /** audits of the first snapshot */
    NvramSnapshot start;
    long lastEval;
    final Map<Long, List<AchievementUnlock>> liveUnlocks = new HashMap<>();

    LiveRound(ScoreServerGame game) {
      this.game = game;
    }
  }

  public void addChangeListener(@NonNull AchievementsChangeListener listener) {
    listeners.add(listener);
  }

  @NonNull
  public AchievementsSettings getSettings() {
    return settings;
  }

  //------------------------------------------- TableStatusChangeListener ---------------------------------------------

  @Override
  public void tableLaunched(TableStatusChangedEvent event) {
    try {
      Game game = event.getGame();
      if (!settings.isEnabled()) {
        return;
      }
      String rom = AchievementGameIndex.romOf(game);
      if (rom == null || getTrackedPlayers().isEmpty()) {
        return;
      }
      if (!ruleStore.isSupported(rom)) {
        LOG.info("Achievements: no NVRAM map for ROM '{}', \"{}\" is not tracked.", rom, game.getGameDisplayName());
        return;
      }
      File nvFile = folderLookupService.getNvRamFile(game);
      NvramSnapshot start = ruleStore.read(rom, nvFile);
      if (start.isEmpty()) {
        LOG.info("Achievements: no readable .nv file for '{}' yet, the first game of this ROM is not tracked.", rom);
        return;
      }
      ruleStore.ensureRomRules(rom);
      ActiveSession session = new ActiveSession(game.getId(), rom, nvFile, nvFile.lastModified(), start, Instant.now());
      activeSessions.put(game.getId(), session);
      if (scoreServer.isRunning()) {
        live = new LiveTable(game, session);
      }
      LOG.info("Achievements: tracking \"{}\" ({}), {} audit fields{}", game.getGameDisplayName(), rom, start.audits().size(),
          scoreServer.isRunning() ? ", live tracking on" : "");
    }
    catch (Exception e) {
      LOG.error("Achievements: failed to start session: {}", e.getMessage(), e);
    }
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    Game game = event.getGame();
    if (settings.isLiveTrackingEnabled() && VpxPluginStatus.BUSY.equals(pluginInstaller.getStatus().state)) {
      // the plugins could not be installed while VPX was running
      executor.submit(pluginInstaller::ensure);
    }
    ActiveSession session = activeSessions.remove(game.getId());
    if (session == null) {
      return;
    }
    Instant endedAt = Instant.now();
    executor.submit(() -> {
      try {
        LiveTable table = live;
        if (table != null && table.session == session) {
          live = null;
          finishLiveSession(game, session, table, endedAt);
        }
        else {
          finishSession(game, session, endedAt, null, null);
        }
      }
      catch (Exception e) {
        LOG.error("Achievements: evaluation failed for {}: {}", session.rom(), e.getMessage(), e);
      }
      finally {
        scoreServer.clearFinishedGames();
      }
    });
  }

  /**
   * End of a table played with the score-server plugin. With the round protocol, the finished games were evaluated
   * already, only a game still running when the table was closed is evaluated. Otherwise the whole session is
   * evaluated like without the plugin, the plugin only tells the number of players.
   */
  private void finishLiveSession(Game game, ActiveSession session, LiveTable table, Instant endedAt) throws InterruptedException {
    if (table.roundsMode) {
      LiveRound round = table.round;
      table.round = null;
      if (round == null || round.start == null) {
        LOG.info("Achievements: the games of {} were evaluated one by one, no game was running at table exit.", session.rom());
        return;
      }
      LOG.info("Achievements: table {} closed during a game, evaluating it.", session.rom());
      finishSession(game, session, endedAt, round, GameExtras.of(round.game, round));
      return;
    }
    List<ScoreServerGame> games = scoreServer.getGamesSince(session.startedAt());
    GameExtras extras = null;
    if (!games.isEmpty()) {
      int players = games.stream().mapToInt(ScoreServerGame::getPlayers).max().orElse(1);
      ScoreServerGame last = games.get(games.size() - 1);
      extras = new GameExtras(players, games.size() == 1 ? last.getBestBall(1) : null,
          games.size() == 1 ? last.getScores() : Collections.emptyMap(), false, Collections.emptyMap());
    }
    finishSession(game, session, endedAt, null, extras);
  }

  private void finishSession(Game game, ActiveSession session, Instant endedAt, @Nullable LiveRound round, @Nullable GameExtras extras)
      throws InterruptedException {
    Instant startedAt = round != null ? round.game.getStarted() : session.startedAt();
    long duration = Math.max(0, (endedAt.toEpochMilli() - startedAt.toEpochMilli()) / 1000);
    if (duration < settings.getMinSessionSeconds()) {
      LOG.info("Achievements: game of {} lasted {}s, minimum is {}s, ignored.", session.rom(), duration, settings.getMinSessionSeconds());
      return;
    }

    // VPinMAME writes the .nv file when the emulator exits, wait for it
    File nvFile = session.nvFile() != null ? session.nvFile() : folderLookupService.getNvRamFile(game);
    long waited = 0;
    while (nvFile != null && nvFile.lastModified() == session.nvModified() && waited < NV_FLUSH_TIMEOUT_MS) {
      Thread.sleep(500);
      waited += 500;
    }
    NvramSnapshot end = ruleStore.read(session.rom(), nvFile);
    if (end.isEmpty()) {
      LOG.warn("Achievements: could not read end audits for {}", session.rom());
      return;
    }
    NvramSnapshot start = round != null ? round.start : session.start();
    evaluateGame(game, session.rom(), start, end, startedAt, endedAt, duration, extras);
  }

  //------------------------------------------- Live tracking (score-server plugin) -----------------------------------

  private class ScoreServerListener implements ScoreServerClient.Listener {
    @Override
    public void roundStarted(@NonNull ScoreServerGame game) {
      executor.submit(() -> {
        LiveTable table = live;
        if (table == null) {
          return;
        }
        if (table.round != null && table.round.game.isRunning()) {
          LOG.info("Achievements: the end of the previous game of {} was missed.", table.session.rom());
        }
        table.roundsMode = true;
        table.round = new LiveRound(game);
        LOG.info("Achievements: new game started on {}", table.session.rom());
      });
    }

    @Override
    public void snapshot(@NonNull ScoreServerGame game, ScoreServerGame.@NonNull Snapshot snapshot) {
      executor.submit(() -> {
        try {
          onSnapshot(game, snapshot);
        }
        catch (Exception e) {
          LOG.error("Achievements: live evaluation failed: {}", e.getMessage(), e);
        }
      });
    }

    @Override
    public void roundEnded(@NonNull ScoreServerGame game) {
      executor.submit(() -> {
        try {
          onRoundEnded(game);
        }
        catch (Exception e) {
          LOG.error("Achievements: evaluation of the game failed: {}", e.getMessage(), e);
        }
      });
    }
  }

  private void onSnapshot(ScoreServerGame game, ScoreServerGame.Snapshot snapshot) {
    LiveTable table = live;
    LiveRound round = table != null ? table.round : null;
    if (round == null || round.game != game) {
      return;
    }
    NvramSnapshot audits = decodeLive(table, snapshot.data());
    if (audits.isEmpty()) {
      return;
    }
    if (round.start == null) {
      round.start = audits; // first snapshot of the game = baseline
      return;
    }
    // live achievements for single player games only: during ball 1 another player can still join
    if (snapshot.players() > 1 || snapshot.ball() < 2) {
      return;
    }
    long now = System.currentTimeMillis();
    if (now - round.lastEval < LIVE_EVAL_MS) {
      return;
    }
    round.lastEval = now;
    evaluateLive(table, round, audits, Math.max(0, (now - game.getStarted().toEpochMilli()) / 1000));
  }

  private void onRoundEnded(ScoreServerGame game) {
    LiveTable table = live;
    LiveRound round = table != null ? table.round : null;
    if (round == null || round.game != game) {
      LOG.info("Achievements: end of a game without a tracked start, ignored.");
      return;
    }
    table.round = null;
    Instant ended = game.getEnded() != null ? game.getEnded() : Instant.now();
    long duration = Math.max(0, (ended.toEpochMilli() - game.getStarted().toEpochMilli()) / 1000);
    if (duration < settings.getMinSessionSeconds()) {
      LOG.info("Achievements: game of {} lasted {}s, minimum is {}s, ignored.", table.session.rom(), duration, settings.getMinSessionSeconds());
      return;
    }
    List<ScoreServerGame.Snapshot> snapshots = game.getSnapshots();
    if (snapshots.isEmpty()) {
      LOG.warn("Achievements: no live NVRAM for the game of {}, not evaluated.", table.session.rom());
      return;
    }
    NvramSnapshot end = decodeLive(table, snapshots.get(snapshots.size() - 1).data());
    if (round.start == null) {
      round.start = decodeLive(table, snapshots.get(0).data());
    }
    if (end.isEmpty() || round.start.isEmpty()) {
      LOG.warn("Achievements: could not decode the live NVRAM of {}, game not evaluated.", table.session.rom());
      return;
    }
    LOG.info("Achievements: game of {} finished ({}s, {} player(s))", table.session.rom(), duration, game.getPlayers());
    evaluateGame(table.game, table.session.rom(), round.start, end, game.getStarted(), ended, duration, GameExtras.of(game, round));
  }

  /**
   * Decodes a live NVRAM snapshot with the map of the ROM: the snapshot is copied into the .nv file read at table start.
   */
  @NonNull
  private NvramSnapshot decodeLive(@NonNull LiveTable table, byte @NonNull [] data) {
    try {
      if (table.nvRaw == null) {
        File nvFile = table.session.nvFile();
        table.nvRaw = nvFile != null && nvFile.isFile() ? java.nio.file.Files.readAllBytes(nvFile.toPath()) : new byte[0];
      }
      if (table.nvRaw.length == 0) {
        return NvramSnapshot.empty();
      }
      if (table.offset == null) {
        if (table.alignAttempts >= MAX_ALIGN_ATTEMPTS) {
          return NvramSnapshot.empty();
        }
        table.alignAttempts++;
        table.offset = LiveNvram.align(table.nvRaw, data);
        if (table.offset == null) {
          if (table.alignAttempts == MAX_ALIGN_ATTEMPTS) {
            LOG.warn("Achievements: the live NVRAM of {} was not found in the .nv file, no live tracking.", table.session.rom());
          }
          return NvramSnapshot.empty();
        }
      }
      return ruleStore.read(table.session.rom(), LiveNvram.merge(table.nvRaw, table.offset, data));
    }
    catch (Exception e) {
      LOG.warn("Achievements: failed to decode the live NVRAM: {}", e.getMessage());
      return NvramSnapshot.empty();
    }
  }

  /**
   * Checks the achievements of the running game and stores the reached ones right away. Tallies are not updated,
   * that happens when the game is evaluated at its end.
   */
  private synchronized void evaluateLive(@NonNull LiveTable table, @NonNull LiveRound round, @NonNull NvramSnapshot audits, long durationSec) {
    String rom = table.session.rom();
    for (Player player : getTrackedPlayers()) {
      long playerId = player.getId();
      List<AchievementUnlock> unlocks = unlockRepository.findByPlayerId(playerId);
      PlayerProgress progress = stateStore.toProgress(stateStore.getState(playerId), unlocks);
      RuleEvaluator.Result result = RuleEvaluator.evaluate(input(rom, round.start, audits, durationSec, false), progress);
      if (result.awards.isEmpty()) {
        continue;
      }
      List<AchievementUnlock> created = new ArrayList<>();
      for (RuleEvaluator.Award award : result.awards) {
        created.add(newUnlock(playerId, award, rom, table.game, Instant.now(), null));
      }
      unlockRepository.saveAll(created);
      round.liveUnlocks.computeIfAbsent(playerId, p -> new ArrayList<>()).addAll(created);
      LOG.info("Achievements: {} ({}) unlocked {} achievement(s) on {} during the game", player.getName(), player.getInitials(), created.size(), rom);
      notifyPlayer(table.game, player, created, Collections.emptyList(), null);
    }
  }

  private RuleEvaluator.Input input(String rom, NvramSnapshot start, NvramSnapshot end, long durationSec, boolean multiplayer) {
    RuleEvaluator.Input in = new RuleEvaluator.Input();
    in.rom = rom;
    in.startAudits = start.audits();
    in.endAudits = end.audits();
    in.durationSec = durationSec;
    in.multiplayer = multiplayer;
    in.globalRules = ruleStore.getGlobalRules();
    in.romRules = ruleStore.ensureRomRules(rom);
    in.manufacturerOfRom = gameIndex::manufacturerOfRom;
    in.installedRoms = gameIndex::installedRoms;
    return in;
  }

  private static AchievementUnlock newUnlock(long playerId, RuleEvaluator.Award award, String rom, @Nullable Game game,
                                             Instant unlockedAt, @Nullable Long sessionId) {
    AchievementUnlock u = new AchievementUnlock();
    u.setPlayerId(playerId);
    u.setTitle(award.title());
    u.setAchKey(AchievementKeys.key(award.title()));
    u.setScope(award.scope());
    u.setRom("rom".equals(award.scope()) ? rom : null);
    u.setGameId(game != null ? game.getId() : null);
    if (game != null && "rom".equals(award.scope())) {
      u.setVpsTableId(game.getExtTableId());
      u.setVpsVersionId(game.getExtTableVersionId());
    }
    u.setUnlockedAt(unlockedAt);
    u.setSessionId(sessionId);
    u.setSource(AchievementUnlock.SOURCE_STUDIO);
    return u;
  }

  /**
   * @return the state of the VPX plugins, including whether the plugin is connected right now
   */
  @NonNull
  public VpxPluginStatus getLiveTrackingStatus() {
    VpxPluginStatus status = pluginInstaller.getStatus();
    status.connected = scoreServer.isConnected();
    status.installed = pluginInstaller.isInstalled();
    return status;
  }

  /**
   * Removes the VPX plugins and restores the official PinMAME plugin. The client switches the live tracking off
   * before, otherwise the plugins would be installed again on the next start.
   */
  @NonNull
  public VpxPluginStatus uninstallPlugins() {
    pluginInstaller.uninstall(true);
    return getLiveTrackingStatus();
  }

  /**
   * Installs or repairs the VPX plugins (live tracking must be enabled).
   */
  @NonNull
  public VpxPluginStatus installPlugins() {
    if (!settings.isLiveTrackingEnabled()) {
      return getLiveTrackingStatus();
    }
    pluginInstaller.ensure();
    return getLiveTrackingStatus();
  }

  /**
   * Connects the score-server client to another address, without the plugin installation. For tests only.
   */
  void startScoreServerClient(java.net.@NonNull URI uri) {
    scoreServer.stop();
    scoreServer = new ScoreServerClient(uri, new ScoreServerListener());
    scoreServer.start();
  }

  boolean isScoreServerConnected() {
    return scoreServer.isConnected();
  }

  void stopScoreServerClient() {
    scoreServer.stop();
    live = null;
  }

  /**
   * Waits until the queued evaluations are done. For tests only.
   */
  void awaitEvaluations() throws Exception {
    executor.submit(() -> {
    }).get(30, java.util.concurrent.TimeUnit.SECONDS);
  }

  private void applyLiveTracking(boolean wasEnabled) {
    boolean enabled = settings.isEnabled() && settings.isLiveTrackingEnabled() && OSUtil.isWindows();
    if (enabled) {
      scoreServer.start();
      executor.submit(pluginInstaller::ensure);
    }
    else {
      scoreServer.stop();
      live = null;
      if (wasEnabled) {
        executor.submit(() -> pluginInstaller.uninstall(false));
      }
    }
  }

  //------------------------------------------- Evaluation ------------------------------------------------------------

  /**
   * Evaluates a finished game for all tracked players. Public for tests and manual re-evaluation.
   */
  public List<Long> evaluateGame(@Nullable Game game, @NonNull String rom, @NonNull NvramSnapshot start, @NonNull NvramSnapshot end,
                                 @NonNull Instant startedAt, @NonNull Instant endedAt, long durationSec) {
    return evaluateGame(game, rom, start, end, startedAt, endedAt, durationSec, null);
  }

  /**
   * Evaluates a finished game for all tracked players.
   *
   * @param extras data of the score-server plugin, null without the plugin
   */
  public synchronized List<Long> evaluateGame(@Nullable Game game, @NonNull String rom, @NonNull NvramSnapshot start, @NonNull NvramSnapshot end,
                                              @NonNull Instant startedAt, @NonNull Instant endedAt, long durationSec, @Nullable GameExtras extras) {
    List<Player> players = getTrackedPlayers();
    if (players.isEmpty()) {
      return Collections.emptyList();
    }
    Integer playerCount = extras != null && extras.playerCount() != null ? extras.playerCount() : end.playerCount();
    boolean multiplayer = playerCount != null && playerCount > 1;

    AchievementSession session = new AchievementSession();
    session.setGameId(game != null ? game.getId() : -1);
    session.setRom(rom);
    session.setStartedAt(startedAt);
    session.setEndedAt(endedAt);
    session.setDurationSec(durationSec);
    session.setPlayerCount(playerCount);
    session.setStartAudits(AchievementStateStore.write(start.audits()));
    session.setEndAudits(AchievementStateStore.write(end.audits()));
    if (extras != null) {
      session.setLiveTracked(extras.liveTracked());
      if (extras.bestBall() != null) {
        session.setBestBall(extras.bestBall().points());
        session.setBestBallNo(extras.bestBall().ball());
      }
      if (!extras.playerScores().isEmpty()) {
        session.setPlayerScores(AchievementStateStore.write(extras.playerScores()));
      }
    }
    session = sessionRepository.saveAndFlush(session);

    Map<String, Integer> playerUnlocks = new LinkedHashMap<>();
    Set<Long> changed = new HashSet<>();
    Map<String, Long> sessionDeltas = Collections.emptyMap();

    for (Player player : players) {
      long playerId = player.getId();
      AchievementPlayerState state = stateStore.getState(playerId);
      List<AchievementUnlock> unlocks = unlockRepository.findByPlayerId(playerId);

      // achievements unlocked during the game: evaluated again (tallies), but not stored twice
      List<AchievementUnlock> liveUnlocks = extras != null ? extras.liveUnlocks().getOrDefault(playerId, Collections.emptyList()) : Collections.emptyList();
      Set<String> liveTitles = new HashSet<>();
      Set<Long> liveIds = new HashSet<>();
      for (AchievementUnlock u : liveUnlocks) {
        liveTitles.add(u.getTitle());
        liveIds.add(u.getId());
      }
      List<AchievementUnlock> before = unlocks.stream().filter(u -> !liveIds.contains(u.getId())).toList();
      PlayerProgress progress = stateStore.toProgress(state, before);
      int levelBefore = LevelCalculator.compute(uniqueTitles(before)).level;

      RuleEvaluator.Result result = RuleEvaluator.evaluate(input(rom, start, end, durationSec, multiplayer), progress);
      sessionDeltas = result.sessionDeltas;

      List<AchievementUnlock> created = new ArrayList<>();
      for (RuleEvaluator.Award award : result.awards) {
        if (!liveTitles.contains(award.title())) {
          created.add(newUnlock(playerId, award, rom, game, endedAt, session.getId()));
        }
      }
      unlockRepository.saveAll(created);
      for (AchievementUnlock u : unlocks) {
        if (liveIds.contains(u.getId())) {
          u.setSessionId(session.getId());
          unlockRepository.save(u);
        }
      }

      // like the Watcher: a retriggered achievement backfills a missing VPS id
      if (game != null && game.getExtTableId() != null && !result.retriggered.isEmpty()) {
        Set<String> retriggered = new HashSet<>(result.retriggered);
        for (AchievementUnlock u : unlocks) {
          if (retriggered.contains(u.getTitle()) && "rom".equals(u.getScope()) && u.getVpsTableId() == null) {
            u.setVpsTableId(game.getExtTableId());
            u.setVpsVersionId(game.getExtTableVersionId());
            u.setSyncedAt(null);
            unlockRepository.save(u);
          }
        }
      }

      stateStore.applyProgress(state, progress);
      if (!multiplayer) {
        stateStore.updateFieldBest(state, rom, result.sessionDeltas);
      }

      List<AchievementUnlock> all = new ArrayList<>(unlocks);
      all.addAll(created);
      List<BadgeInfo> newBadges = updateBadges(state, all);
      state.setSummaryDirty(true);
      stateStore.save(state);

      playerUnlocks.put(String.valueOf(playerId), created.size() + liveIds.size());
      changed.add(playerId);
      LOG.info("Achievements: {} ({}) unlocked {} achievement(s) on {}{}{}", player.getName(), player.getInitials(), created.size(), rom,
          liveIds.isEmpty() ? "" : " (+" + liveIds.size() + " during the game)", multiplayer ? " (multiplayer game)" : "");

      int levelAfter = LevelCalculator.compute(uniqueTitles(all)).level;
      notifyPlayer(game, player, created, newBadges, levelAfter > levelBefore ? LevelCalculator.compute(uniqueTitles(all)) : null);
    }

    session.setDeltas(AchievementStateStore.write(sessionDeltas));
    session.setPlayerUnlocks(AchievementStateStore.write(playerUnlocks));
    sessionRepository.saveAndFlush(session);

    for (AchievementsChangeListener listener : listeners) {
      try {
        listener.achievementsChanged(changed);
      }
      catch (Exception e) {
        LOG.error("Achievements change listener failed: {}", e.getMessage(), e);
      }
    }
    return new ArrayList<>(changed);
  }

  //------------------------------------------- Badges ----------------------------------------------------------------

  /**
   * Evaluates the badges and stores newly earned ones.
   *
   * @return the newly earned badges
   */
  @NonNull
  public List<BadgeInfo> updateBadges(@NonNull AchievementPlayerState state, @NonNull List<AchievementUnlock> unlocks) {
    Set<String> earned = stateStore.getBadges(state);
    List<BadgeInfo> badges = BadgeEvaluator.evaluate(computeBadgeStats(state, unlocks), earned, state.getSelectedBadge());
    List<BadgeInfo> newOnes = new ArrayList<>();
    for (BadgeInfo b : badges) {
      if (b.earned && !earned.contains(b.id)) {
        earned.add(b.id);
        newOnes.add(b);
      }
    }
    if (!newOnes.isEmpty()) {
      stateStore.setBadges(state, earned);
    }
    return newOnes;
  }

  @NonNull
  public BadgeStats computeBadgeStats(@NonNull AchievementPlayerState state, @NonNull List<AchievementUnlock> unlocks) {
    BadgeStats s = new BadgeStats();
    s.totalAchievements = uniqueTitles(unlocks);
    LevelInfo level = LevelCalculator.compute(s.totalAchievements);
    s.level = level.level;
    s.prestige = level.prestige;
    s.fullyMaxed = level.fullyMaxed;
    s.romsPlayed = new HashSet<>(stateStore.getRomsPlayed(state));
    for (String rom : s.romsPlayed) {
      String mfr = gameIndex.manufacturerOfRom(rom);
      if (mfr != null) {
        s.manufacturerRoms.computeIfAbsent(mfr, m -> new HashSet<>()).add(rom);
      }
    }
    s.totalPlaytimeSec = getAlxPlaytimeSec();
    s.cloudUploadDone = state.isCloudUploadDone();

    String playerKey = String.valueOf(state.getPlayerId());
    for (AchievementSession session : sessionRepository.findAll()) {
      Map<String, Integer> pu = AchievementStateStore.read(session.getPlayerUnlocks(), new tools.jackson.core.type.TypeReference<Map<String, Integer>>() {
      });
      if (pu == null || !pu.containsKey(playerKey)) {
        continue;
      }
      int count = pu.get(playerKey);
      s.maxSessionSec = Math.max(s.maxSessionSec, session.getDurationSec());
      s.maxSessionUnlocks = Math.max(s.maxSessionUnlocks, count);
      // achievements are evaluated at the end of a game: a game of at most 5 minutes counts for Speed Demon
      if (session.getDurationSec() <= 300) {
        s.maxUnlocksIn5Min = Math.max(s.maxUnlocksIn5Min, count);
      }
      int hour = ZonedDateTime.ofInstant(session.getStartedAt(), ZoneId.systemDefault()).getHour();
      if (hour < 5) {
        s.nightOwl = true;
      }
    }

    for (AchievementUnlock u : unlocks) {
      if (u.getRom() == null) {
        continue;
      }
      AchievementRarityCache.Rarity rarity = rarityCache.get(u.getRom());
      if (rarity == null) {
        continue;
      }
      RarityTier tier = rarity.tierOf(u.getAchKey());
      if (tier == RarityTier.Legendary) s.hasLegendary = true;
      else if (tier == RarityTier.Epic) s.hasEpic = true;
      else if (tier == RarityTier.Rare) s.hasRare = true;
    }
    return s;
  }

  public long getAlxPlaytimeSec() {
    if (alxPlaytimeSec >= 0 && System.currentTimeMillis() - alxFetchedAt < ALX_CACHE_MS) {
      return alxPlaytimeSec;
    }
    long total = 0;
    try {
      AlxSummary summary = alxService.getAlxSummary();
      if (summary != null) {
        for (TableAlxEntry entry : summary.getEntries()) {
          total += entry.getTimePlayedSecs();
        }
      }
    }
    catch (Exception e) {
      LOG.warn("Achievements: failed to read the frontend playtime: {}", e.getMessage());
    }
    alxPlaytimeSec = total;
    alxFetchedAt = System.currentTimeMillis();
    return total;
  }

  //------------------------------------------- Notifications ---------------------------------------------------------

  private void notifyPlayer(@Nullable Game game, Player player, List<AchievementUnlock> created, List<BadgeInfo> newBadges, @Nullable LevelInfo levelUp) {
    try {
      NotificationSettings notificationSettings = preferencesService.getJsonPreference(PreferenceNames.NOTIFICATION_SETTINGS, NotificationSettings.class);
      if (notificationSettings != null && !notificationSettings.isAchievementsNotification()) {
        return;
      }
      // shown on the cabinet, so always in the local UI language
      Locale locale = ServerMessages.resolveLocalLocale();
      File wheel = game != null ? frontendService.getWheelImage(game) : null;
      String gameName = game != null ? game.getGameDisplayName() : "";
      String who = player.getName() != null ? player.getName() : player.getInitials();
      if (!created.isEmpty()) {
        String first = AchievementKeys.displayTitle(created.get(0).getTitle(), created.get(0).getRom());
        String text = created.size() == 1 ? first : ServerMessages.get("achievements.notification.more", locale, first, created.size() - 1);
        String title = created.size() == 1
            ? ServerMessages.get("achievements.notification.unlocked_one", locale, who)
            : ServerMessages.get("achievements.notification.unlocked_many", locale, who, created.size());
        notificationService.showNotification(NotificationFactory.createNotification(wheel, gameName, "🏆 " + title, text));
      }
      for (BadgeInfo badge : newBadges) {
        Notification n = NotificationFactory.createNotification(null, gameName,
            "🏅 " + ServerMessages.get("achievements.notification.badge", locale, who), badgeName(badge, locale));
        String category = badge.category;
        n.setGraphic(() -> BadgeMedal.create(category, true, BADGE_NOTIFICATION_SIZE));
        notificationService.showNotification(n);
      }
      if (levelUp != null) {
        Notification n = NotificationFactory.createNotification(wheel, gameName,
            "⭐ " + ServerMessages.get("achievements.notification.level_up", locale, who),
            ServerMessages.get("achievements.notification.level", locale, levelUp.level, levelUp.name));
        notificationService.showNotification(n);
      }
    }
    catch (Exception e) {
      LOG.warn("Achievements: failed to show notification: {}", e.getMessage());
    }
  }

  /**
   * Localized badge name from the UI bundle (the badge names are translated there), falls back to the English name.
   */
  private static String badgeName(BadgeInfo badge, Locale locale) {
    try {
      ResourceBundle bundle = ResourceBundle.getBundle(UI_BUNDLE, locale, ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
      return bundle.getString("achievements.badge." + badge.id + ".name");
    }
    catch (MissingResourceException e) {
      return badge.name;
    }
  }

  //------------------------------------------- Helpers ---------------------------------------------------------------

  @NonNull
  public List<Player> getTrackedPlayers() {
    List<Player> result = new ArrayList<>();
    for (Player p : playerService.getBuildInPlayers()) {
      if (p.isAchievementsEnabled() && p.getId() != null) {
        result.add(p);
      }
    }
    return result;
  }

  public static int uniqueTitles(@NonNull List<AchievementUnlock> unlocks) {
    Set<String> titles = new HashSet<>();
    for (AchievementUnlock u : unlocks) {
      titles.add(u.getTitle().strip());
    }
    return titles.size();
  }

  public boolean selectBadge(long playerId, @Nullable String badgeId) {
    AchievementPlayerState state = stateStore.getState(playerId);
    if (badgeId != null && badgeId.isEmpty()) {
      badgeId = null;
    }
    if (badgeId != null) {
      // the UI shows the live evaluation, the stored set is only updated after a game: store badges earned since
      updateBadges(state, unlockRepository.findByPlayerId(playerId));
      if (!stateStore.getBadges(state).contains(badgeId)) {
        LOG.warn("Achievements: player {} has not earned the badge \"{}\"", playerId, badgeId);
        return false;
      }
    }
    state.setSelectedBadge(badgeId);
    state.setSummaryDirty(true);
    stateStore.save(state);
    for (AchievementsChangeListener listener : listeners) {
      listener.achievementsChanged(Set.of(playerId));
    }
    return true;
  }

  /**
   * Deletes all achievement data of a player (when the player is deleted).
   */
  public synchronized void deletePlayerData(long playerId) {
    unlockRepository.deleteAll(unlockRepository.findByPlayerId(playerId));
    if (stateRepository.existsById(playerId)) {
      stateRepository.deleteById(playerId);
    }
    LOG.info("Achievements: deleted the data of player {}", playerId);
  }

  @Override
  public void playerCreated(@NonNull Player player) {
  }

  @Override
  public void playerUpdated(@NonNull Player player) {
  }

  @Override
  public void playerDeleted(@NonNull Player player) {
    if (player.getId() != null) {
      deletePlayerData(player.getId());
    }
  }

  public Map<String, Object> getStatus() {
    Map<String, Object> status = new HashMap<>();
    status.put("enabled", settings.isEnabled());
    status.put("activeSessions", activeSessions.size());
    return status;
  }

  //------------------------------------------- Lifecycle -------------------------------------------------------------

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.ACHIEVEMENTS_SETTINGS.equals(propertyName)) {
      boolean wasEnabled = settings.isEnabled() && settings.isLiveTrackingEnabled();
      loadSettings();
      boolean enabled = settings.isEnabled() && settings.isLiveTrackingEnabled();
      if (enabled != wasEnabled) {
        applyLiveTracking(wasEnabled);
      }
    }
  }

  private void loadSettings() {
    try {
      AchievementsSettings s = preferencesService.getJsonPreference(PreferenceNames.ACHIEVEMENTS_SETTINGS, AchievementsSettings.class);
      settings = s != null ? s : new AchievementsSettings();
    }
    catch (Exception e) {
      LOG.warn("Failed to read achievement settings: {}", e.getMessage());
      settings = new AchievementsSettings();
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    loadSettings();
    preferencesService.addChangeListener(this);
    frontendStatusService.addTableStatusChangeListener(this);
    playerService.addPlayerLifecycleListener(this);
    applyLiveTracking(false);
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
