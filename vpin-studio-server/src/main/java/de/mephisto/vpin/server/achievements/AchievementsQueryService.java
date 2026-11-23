package de.mephisto.vpin.server.achievements;

import de.mephisto.vpin.restclient.achievements.AchievementInfo;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.restclient.achievements.TableAchievements;
import de.mephisto.vpin.restclient.achievements.TableAchievementsDetails;
import de.mephisto.vpin.server.achievements.model.AchievementPlayerState;
import de.mephisto.vpin.server.achievements.model.AchievementSession;
import de.mephisto.vpin.server.achievements.model.AchievementSessionRepository;
import de.mephisto.vpin.server.achievements.model.AchievementUnlock;
import de.mephisto.vpin.server.achievements.model.AchievementUnlockRepository;
import de.mephisto.vpin.server.achievements.nvram.NvramSnapshot;
import de.mephisto.vpin.server.achievements.progression.BadgeEvaluator;
import de.mephisto.vpin.server.achievements.progression.LevelCalculator;
import de.mephisto.vpin.server.achievements.progression.RarityTier;
import de.mephisto.vpin.server.achievements.rules.AchievementRule;
import de.mephisto.vpin.server.achievements.rules.PlayerProgress;
import de.mephisto.vpin.server.achievements.rules.RuleCondition;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds the achievement views of the UI.
 */
@Service
public class AchievementsQueryService {

  public static final String GLOBAL = "__global__";

  @Autowired
  private PlayerService playerService;
  @Autowired
  private AchievementsService achievementsService;
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
  private FolderLookupService folderLookupService;

  @NonNull
  public List<PlayerAchievements> getPlayers() {
    List<PlayerAchievements> result = new ArrayList<>();
    for (Player player : playerService.getBuildInPlayers()) {
      PlayerAchievements p = base(player);
      List<AchievementUnlock> unlocks = unlockRepository.findByPlayerId(player.getId());
      p.unlockedCount = AchievementsService.uniqueTitles(unlocks);
      p.level = LevelCalculator.compute(p.unlockedCount);
      result.add(p);
    }
    return result;
  }

  @Nullable
  public PlayerAchievements getPlayerAchievements(long playerId) {
    Player player = playerService.getBuildInPlayer(playerId);
    if (player == null) {
      return null;
    }
    PlayerAchievements p = base(player);
    AchievementPlayerState state = stateStore.getState(playerId);
    List<AchievementUnlock> unlocks = unlockRepository.findByPlayerId(playerId);
    p.unlockedCount = AchievementsService.uniqueTitles(unlocks);
    p.level = LevelCalculator.compute(p.unlockedCount);
    p.levels = LevelCalculator.LEVELS;
    Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
    p.unlockedThisWeek = (int) unlocks.stream().filter(u -> u.getUnlockedAt() != null && u.getUnlockedAt().isAfter(weekAgo)).count();
    p.selectedBadge = state.getSelectedBadge();
    p.badges = BadgeEvaluator.evaluate(achievementsService.computeBadgeStats(state, unlocks), stateStore.getBadges(state), state.getSelectedBadge());
    p.badgesTotal = p.badges.size();
    p.badgesEarned = (int) p.badges.stream().filter(b -> b.earned).count();
    p.lastSync = state.getLastSync() != null ? Date.from(state.getLastSync()) : null;
    p.migratedFirebasePid = state.getMigratedFirebasePid();
    p.pendingUploads = player.getManiaAccountUuid() == null ? 0 :
        (int) unlocks.stream().filter(u -> u.getSyncedAt() == null && u.isUploadable()).count();

    List<AchievementUnlock> sorted = new ArrayList<>(unlocks);
    sorted.sort(Comparator.comparing(AchievementUnlock::getUnlockedAt, Comparator.nullsLast(Comparator.reverseOrder())));
    for (AchievementUnlock u : sorted.subList(0, Math.min(8, sorted.size()))) {
      p.recentUnlocks.add(toInfo(u));
    }
    p.closest = closest(state, unlocks, 5);
    return p;
  }

  private PlayerAchievements base(Player player) {
    PlayerAchievements p = new PlayerAchievements();
    p.playerId = player.getId();
    p.name = player.getName();
    p.initials = player.getInitials();
    p.maniaAccountUuid = player.getManiaAccountUuid();
    p.tracked = player.isAchievementsEnabled();
    return p;
  }

  private AchievementInfo toInfo(AchievementUnlock u) {
    AchievementInfo info = new AchievementInfo();
    info.title = u.getTitle();
    info.rom = u.getRom() != null ? u.getRom() : AchievementKeys.romOfTitle(u.getTitle());
    info.displayTitle = AchievementKeys.displayTitle(u.getTitle(), info.rom);
    info.tableName = gameIndex.tableName(info.rom);
    info.scope = u.getScope();
    info.unlocked = true;
    info.unlockedAt = u.getUnlockedAt() != null ? Date.from(u.getUnlockedAt()) : null;
    applyRarity(info, info.rom, u.getAchKey());
    return info;
  }

  private void applyRarity(AchievementInfo info, @Nullable String rom, String achKey) {
    AchievementRarityCache.Rarity rarity = rarityCache.get(rom);
    if (rarity != null) {
      RarityTier tier = rarity.tierOf(achKey);
      info.rarity = tier != null ? tier.name() : null;
      info.rarityPct = rarity.pctOf(achKey);
    }
  }

  /**
   * Locked achievements with the highest progress.
   */
  private List<AchievementInfo> closest(AchievementPlayerState state, List<AchievementUnlock> unlocks, int max) {
    Set<String> unlocked = new HashSet<>();
    unlocks.forEach(u -> unlocked.add(u.getTitle()));
    Map<String, PlayerProgress.Tally> tally = stateStore.getTally(state);
    List<AchievementInfo> candidates = new ArrayList<>();

    for (AchievementRule rule : ruleStore.getGlobalRules()) {
      String title = rule.getTitleOrDefault();
      PlayerProgress.Tally t = tally.get(title);
      if (unlocked.contains(title) || t == null || t.progress <= 0) {
        continue;
      }
      long target = globalTarget(rule.getCondition(), t);
      if (target <= 0 || t.progress >= target) {
        continue;
      }
      AchievementInfo info = lockedInfo(rule, null);
      info.progressCurrent = t.progress;
      info.progressTarget = target;
      candidates.add(info);
    }

    Map<String, Map<String, Long>> best = stateStore.getFieldBest(state);
    for (Map.Entry<String, Map<String, Long>> e : best.entrySet()) {
      List<AchievementRule> rules = ruleStore.getRomRules(e.getKey());
      if (rules == null) {
        continue;
      }
      for (AchievementRule rule : rules) {
        RuleCondition c = rule.getCondition();
        if (unlocked.contains(rule.getTitleOrDefault()) || c == null || !RuleCondition.NVRAM_DELTA.equals(c.getType())) {
          continue;
        }
        long value = e.getValue().getOrDefault(c.getField(), 0L);
        int min = c.minOrDefault(1);
        if (value <= 0 || value >= min) {
          continue;
        }
        AchievementInfo info = lockedInfo(rule, e.getKey());
        info.progressCurrent = value;
        info.progressTarget = min;
        candidates.add(info);
      }
    }
    candidates.sort(Comparator.comparingDouble(AchievementInfo::getProgressPct).reversed());
    return new ArrayList<>(candidates.subList(0, Math.min(max, candidates.size())));
  }

  private static long globalTarget(@Nullable RuleCondition c, PlayerProgress.Tally t) {
    if (c == null || c.getType() == null) {
      return 0;
    }
    return switch (c.getType()) {
      case RuleCondition.ROM_COMPLETE_SET, RuleCondition.ROM_MULTI_BRAND -> t.installedCount != null ? t.installedCount : 0;
      case RuleCondition.ROM_COUNT -> c.getMinBrands() != null ? c.getMinBrands() : c.minOrDefault(1);
      default -> c.minOrDefault(1);
    };
  }

  private AchievementInfo lockedInfo(AchievementRule rule, @Nullable String rom) {
    AchievementInfo info = new AchievementInfo();
    info.title = rule.getTitleOrDefault();
    info.rom = rom;
    info.displayTitle = AchievementKeys.displayTitle(info.title, rom);
    info.tableName = gameIndex.tableName(rom);
    info.scope = rule.isGlobal() ? AchievementRule.SCOPE_GLOBAL : "rom";
    info.condition = rule.getCondition() != null ? rule.getCondition().describe() : null;
    applyRarity(info, rom, AchievementKeys.key(info.title));
    return info;
  }

  //------------------------------------------------- Tables --------------------------------------------------------

  @NonNull
  public List<TableAchievements> getTables(long playerId) {
    List<AchievementUnlock> unlocks = unlockRepository.findByPlayerId(playerId);
    Map<String, Set<String>> unlockedByRom = new HashMap<>();
    for (AchievementUnlock u : unlocks) {
      if (u.getRom() != null) {
        unlockedByRom.computeIfAbsent(u.getRom(), r -> new HashSet<>()).add(u.getTitle());
      }
    }
    Map<String, Instant> lastPlayed = lastPlayedByRom(playerId);

    AchievementGameIndex.Snapshot index = gameIndex.get();
    List<TableAchievements> result = new ArrayList<>();
    for (Map.Entry<String, List<Game>> e : index.gamesByRom.entrySet()) {
      result.add(toTable(e.getKey(), e.getValue().get(0), index, unlockedByRom, lastPlayed));
    }
    // played tables first, then tables with achievements, tables without NVRAM map at the end
    result.sort(Comparator.comparing((TableAchievements t) -> t.lastPlayed, Comparator.nullsLast(Comparator.reverseOrder()))
        .thenComparing(t -> !(t.hasMap && t.total > 0))
        .thenComparing(t -> !t.hasMap)
        .thenComparing(t -> t.gameName == null ? "" : t.gameName.toLowerCase()));
    return result;
  }

  private TableAchievements toTable(String rom, Game game, AchievementGameIndex.Snapshot index, Map<String, Set<String>> unlockedByRom,
                                    Map<String, Instant> lastPlayed) {
    TableAchievements t = new TableAchievements();
    t.gameId = game.getId();
    t.gameName = game.getGameDisplayName();
    t.rom = rom;
    t.vpsTableId = game.getExtTableId();
    t.vpsVersionId = game.getExtTableVersionId();
    t.hasMap = index.romsWithMap.contains(rom);
    t.mapRom = t.hasMap ? ruleStore.getAuditReader().resolveMapRom(rom) : null;
    Instant lp = lastPlayed.get(rom);
    t.lastPlayed = lp != null ? Date.from(lp) : null;
    if (t.hasMap) {
      List<AchievementRule> rules = rulesFor(rom, game);
      t.total = rules.size();
      Set<String> titles = unlockedByRom.getOrDefault(rom, Set.of());
      t.unlocked = (int) rules.stream().filter(r -> titles.contains(r.getTitleOrDefault())).count();
    }
    t.status = !t.hasMap ? TableAchievements.STATUS_NO_MAP
        : (t.total > 0 && t.unlocked >= t.total) ? TableAchievements.STATUS_COMPLETED
        : t.unlocked > 0 ? TableAchievements.STATUS_IN_PROGRESS : TableAchievements.STATUS_NOT_STARTED;
    return t;
  }

  @Nullable
  private TableAchievements getTable(long playerId, String rom) {
    AchievementGameIndex.Snapshot index = gameIndex.get();
    List<Game> games = index.gamesByRom.get(rom);
    if (games == null || games.isEmpty()) {
      return null;
    }
    Map<String, Set<String>> unlockedByRom = new HashMap<>();
    for (AchievementUnlock u : unlockRepository.findByPlayerIdAndRom(playerId, rom)) {
      unlockedByRom.computeIfAbsent(rom, r -> new HashSet<>()).add(u.getTitle());
    }
    return toTable(rom, games.get(0), index, unlockedByRom, lastPlayedByRom(playerId));
  }

  /**
   * The rules of a ROM; generated from the current .nv file if they do not exist yet (the Watcher generates them
   * the first time the audits are available).
   */
  private List<AchievementRule> rulesFor(String rom, Game game) {
    List<AchievementRule> rules = ruleStore.getRomRules(rom);
    if (rules != null) {
      return rules;
    }
    try {
      NvramSnapshot snapshot = ruleStore.getAuditReader().read(rom, folderLookupService.getNvRamFile(game));
      return ruleStore.ensureRomRules(rom, snapshot);
    }
    catch (Exception e) {
      return List.of();
    }
  }

  private Map<String, Instant> lastPlayedByRom(long playerId) {
    Map<String, Instant> result = new HashMap<>();
    String key = String.valueOf(playerId);
    for (AchievementSession s : sessionRepository.findAll()) {
      if (s.getRom() == null || s.getPlayerUnlocks() == null || !s.getPlayerUnlocks().contains("\"" + key + "\"")) {
        continue;
      }
      Instant ended = s.getEndedAt() != null ? s.getEndedAt() : s.getStartedAt();
      result.merge(s.getRom(), ended, (a, b) -> a.isAfter(b) ? a : b);
    }
    return result;
  }

  @NonNull
  public TableAchievementsDetails getTableDetails(long playerId, @NonNull String rom) {
    String key = rom.toLowerCase();
    TableAchievementsDetails details = new TableAchievementsDetails();
    List<AchievementUnlock> unlocks = unlockRepository.findByPlayerId(playerId);
    Map<String, AchievementUnlock> byTitle = new LinkedHashMap<>();
    unlocks.forEach(u -> byTitle.put(u.getTitle(), u));

    List<AchievementRule> rules;
    if (GLOBAL.equals(key)) {
      rules = ruleStore.getGlobalRules();
      TableAchievements t = new TableAchievements();
      t.rom = GLOBAL;
      t.gameName = "Global achievements";
      t.hasMap = true;
      details.table = t;
    }
    else {
      details.table = getTable(playerId, key);
      List<AchievementRule> r = ruleStore.getRomRules(key);
      rules = r != null ? r : List.of();
    }

    AchievementPlayerState state = stateStore.getState(playerId);
    Map<String, Long> best = stateStore.getFieldBest(state).getOrDefault(key, Map.of());
    Map<String, PlayerProgress.Tally> tally = stateStore.getTally(state);
    String rarityRom = GLOBAL.equals(key) ? null : key;
    details.rarityAvailable = rarityRom != null && rarityCache.get(rarityRom) != null;
    for (String tier : List.of("Common", "Uncommon", "Rare", "Epic", "Legendary")) {
      details.rarityDistribution.put(tier, 0);
    }

    Set<String> ruleTitles = new HashSet<>();
    int unlockedCount = 0;
    for (AchievementRule rule : rules) {
      String title = rule.getTitleOrDefault();
      ruleTitles.add(title);
      AchievementInfo info = lockedInfo(rule, rarityRom);
      AchievementUnlock u = byTitle.get(title);
      if (u != null) {
        info.unlocked = true;
        info.unlockedAt = u.getUnlockedAt() != null ? Date.from(u.getUnlockedAt()) : null;
        unlockedCount++;
        if (info.rarity != null) {
          details.rarityDistribution.merge(info.rarity, 1, Integer::sum);
        }
      }
      else {
        RuleCondition c = rule.getCondition();
        if (c != null && RuleCondition.NVRAM_DELTA.equals(c.getType())) {
          info.progressCurrent = best.getOrDefault(c.getField(), 0L);
          info.progressTarget = c.minOrDefault(1);
        }
        else if (c != null && rule.isGlobal()) {
          PlayerProgress.Tally t = tally.get(title);
          if (t != null) {
            info.progressCurrent = t.progress;
            info.progressTarget = globalTarget(c, t);
          }
        }
      }
      details.achievements.add(info);
    }

    // unlocked titles of the ROM that are not part of the current rules (e.g. migrated from the Watcher)
    if (!GLOBAL.equals(key)) {
      for (AchievementUnlock u : unlocks) {
        if (key.equals(u.getRom()) && !ruleTitles.contains(u.getTitle())) {
          AchievementInfo info = toInfo(u);
          info.legacy = true;
          details.achievements.add(info);
        }
      }
    }
    if (GLOBAL.equals(key) && details.table != null) {
      details.table.total = rules.size();
      details.table.unlocked = unlockedCount;
    }
    return details;
  }
}
