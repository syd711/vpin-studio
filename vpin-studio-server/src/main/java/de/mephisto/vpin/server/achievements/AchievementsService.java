package de.mephisto.vpin.server.achievements;

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
import de.mephisto.vpin.server.alx.AlxService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.frontend.TableStatusChangeListener;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.TableStatusChangedEvent;
import de.mephisto.vpin.server.games.TableStatusChangedOrigin;
import de.mephisto.vpin.server.notifications.NotificationService;
import de.mephisto.vpin.server.players.Player;
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
import java.util.Map;
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
      if (!settings.isEnabled() || TableStatusChangedOrigin.ORIGIN_STUDIO.equals(event.getOrigin())) {
        return;
      }
      String rom = AchievementGameIndex.romOf(game);
      if (rom == null || getTrackedPlayers().isEmpty()) {
        return;
      }
      if (!ruleStore.getAuditReader().isSupported(rom)) {
        LOG.info("Achievements: no NVRAM map for ROM '{}', \"{}\" is not tracked.", rom, game.getGameDisplayName());
        return;
      }
      File nvFile = folderLookupService.getNvRamFile(game);
      NvramSnapshot start = ruleStore.getAuditReader().read(rom, nvFile);
      if (start.isEmpty()) {
        LOG.info("Achievements: no readable .nv file for '{}' yet, the first game of this ROM is not tracked.", rom);
        return;
      }
      ruleStore.ensureRomRules(rom, start);
      activeSessions.put(game.getId(), new ActiveSession(game.getId(), rom, nvFile, nvFile.lastModified(), start, Instant.now()));
      LOG.info("Achievements: tracking \"{}\" ({}), {} audit fields", game.getGameDisplayName(), rom, start.audits().size());
    }
    catch (Exception e) {
      LOG.error("Achievements: failed to start session: {}", e.getMessage(), e);
    }
  }

  @Override
  public void tableExited(TableStatusChangedEvent event) {
    Game game = event.getGame();
    ActiveSession session = activeSessions.remove(game.getId());
    if (session == null) {
      return;
    }
    Instant endedAt = Instant.now();
    executor.submit(() -> {
      try {
        finishSession(game, session, endedAt);
      }
      catch (Exception e) {
        LOG.error("Achievements: evaluation failed for {}: {}", session.rom(), e.getMessage(), e);
      }
    });
  }

  private void finishSession(Game game, ActiveSession session, Instant endedAt) throws InterruptedException {
    long duration = Math.max(0, (endedAt.toEpochMilli() - session.startedAt().toEpochMilli()) / 1000);
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
    NvramSnapshot end = ruleStore.getAuditReader().read(session.rom(), nvFile);
    if (end.isEmpty()) {
      LOG.warn("Achievements: could not read end audits for {}", session.rom());
      return;
    }
    evaluateGame(game, session.rom(), session.start(), end, session.startedAt(), endedAt, duration);
  }

  /**
   * Evaluates a finished game for all tracked players. Public for tests and manual re-evaluation.
   */
  public synchronized List<Long> evaluateGame(@Nullable Game game, @NonNull String rom, @NonNull NvramSnapshot start, @NonNull NvramSnapshot end,
                                              @NonNull Instant startedAt, @NonNull Instant endedAt, long durationSec) {
    List<Player> players = getTrackedPlayers();
    if (players.isEmpty()) {
      return Collections.emptyList();
    }
    boolean multiplayer = end.playerCount() != null && end.playerCount() > 1;
    List<AchievementRule> romRules = ruleStore.ensureRomRules(rom, start);

    AchievementSession session = new AchievementSession();
    session.setGameId(game != null ? game.getId() : -1);
    session.setRom(rom);
    session.setStartedAt(startedAt);
    session.setEndedAt(endedAt);
    session.setDurationSec(durationSec);
    session.setPlayerCount(end.playerCount());
    session.setStartAudits(AchievementStateStore.write(start.audits()));
    session.setEndAudits(AchievementStateStore.write(end.audits()));
    session = sessionRepository.saveAndFlush(session);

    Map<String, Integer> playerUnlocks = new LinkedHashMap<>();
    Set<Long> changed = new HashSet<>();
    Map<String, Long> sessionDeltas = Collections.emptyMap();

    for (Player player : players) {
      long playerId = player.getId();
      AchievementPlayerState state = stateStore.getState(playerId);
      List<AchievementUnlock> unlocks = unlockRepository.findByPlayerId(playerId);
      PlayerProgress progress = stateStore.toProgress(state, unlocks);
      int levelBefore = LevelCalculator.compute(uniqueTitles(unlocks)).level;

      RuleEvaluator.Input in = new RuleEvaluator.Input();
      in.rom = rom;
      in.startAudits = start.audits();
      in.endAudits = end.audits();
      in.durationSec = durationSec;
      in.multiplayer = multiplayer;
      in.globalRules = ruleStore.getGlobalRules();
      in.romRules = romRules;
      in.manufacturerOfRom = gameIndex::manufacturerOfRom;
      in.installedRoms = gameIndex::installedRoms;
      RuleEvaluator.Result result = RuleEvaluator.evaluate(in, progress);
      sessionDeltas = result.sessionDeltas;

      List<AchievementUnlock> created = new ArrayList<>();
      for (RuleEvaluator.Award award : result.awards) {
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
        u.setUnlockedAt(endedAt);
        u.setSessionId(session.getId());
        u.setSource(AchievementUnlock.SOURCE_STUDIO);
        created.add(u);
      }
      unlockRepository.saveAll(created);

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

      playerUnlocks.put(String.valueOf(playerId), created.size());
      changed.add(playerId);
      LOG.info("Achievements: {} ({}) unlocked {} achievement(s) on {}{}", player.getName(), player.getInitials(), created.size(), rom,
          multiplayer ? " (multiplayer game)" : "");

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
    if (!settings.isNotificationsEnabled()) {
      return;
    }
    try {
      File wheel = game != null ? frontendService.getWheelImage(game) : null;
      String gameName = game != null ? game.getGameDisplayName() : "";
      String who = player.getName() != null ? player.getName() : player.getInitials();
      if (!created.isEmpty()) {
        String first = AchievementKeys.displayTitle(created.get(0).getTitle(), created.get(0).getRom());
        String text = created.size() == 1 ? first : first + " +" + (created.size() - 1) + " more";
        notificationService.showNotification(NotificationFactory.createNotification(wheel, gameName,
            "🏆 " + who + ": " + created.size() + " achievement" + (created.size() > 1 ? "s" : "") + " unlocked", text));
      }
      for (BadgeInfo badge : newBadges) {
        notificationService.showNotification(NotificationFactory.createNotification(wheel, gameName, "🏅 " + who + ": badge unlocked", badge.name));
      }
      if (levelUp != null) {
        Notification n = NotificationFactory.createNotification(wheel, gameName, "⭐ " + who + ": level up!", "Level " + levelUp.level + " – " + levelUp.name);
        notificationService.showNotification(n);
      }
    }
    catch (Exception e) {
      LOG.warn("Achievements: failed to show notification: {}", e.getMessage());
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
    if (badgeId != null && !badgeId.isEmpty() && !stateStore.getBadges(state).contains(badgeId)) {
      return false;
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
      loadSettings();
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
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
