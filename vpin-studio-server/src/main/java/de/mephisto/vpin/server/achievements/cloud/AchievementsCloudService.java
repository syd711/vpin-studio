package de.mephisto.vpin.server.achievements.cloud;

import de.mephisto.vpin.connectors.mania.VPinManiaClient;
import de.mephisto.vpin.connectors.mania.model.AchievementAccountData;
import de.mephisto.vpin.connectors.mania.model.AchievementImportRequest;
import de.mephisto.vpin.connectors.mania.model.AchievementProgress;
import de.mephisto.vpin.connectors.mania.model.AchievementRarity;
import de.mephisto.vpin.connectors.mania.model.AchievementSummary;
import de.mephisto.vpin.connectors.mania.model.AchievementSync;
import de.mephisto.vpin.connectors.mania.model.AchievementSyncResponse;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.restclient.achievements.AchievementsSyncResult;
import de.mephisto.vpin.server.achievements.AchievementGameIndex;
import de.mephisto.vpin.server.achievements.AchievementKeys;
import de.mephisto.vpin.server.achievements.AchievementRarityCache;
import de.mephisto.vpin.server.achievements.AchievementRuleStore;
import de.mephisto.vpin.server.achievements.AchievementStateStore;
import de.mephisto.vpin.server.achievements.AchievementsService;
import de.mephisto.vpin.server.achievements.model.AchievementPlayerState;
import de.mephisto.vpin.server.achievements.model.AchievementUnlock;
import de.mephisto.vpin.server.achievements.model.AchievementUnlockRepository;
import de.mephisto.vpin.server.achievements.rules.AchievementRule;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.mania.ManiaService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import static de.mephisto.vpin.server.VPinStudioServer.Features;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Synchronizes the achievements of cabinet players linked to a VPin Mania account: uploads pending unlocks,
 * per ROM progress and the summary, restores unlocks stored in VPin Mania (e.g. after a reinstallation)
 * and fetches the rarity counters.
 */
@Service
public class AchievementsCloudService implements AchievementsService.AchievementsChangeListener, InitializingBean {
  private static final Logger LOG = LoggerFactory.getLogger(AchievementsCloudService.class);

  @Autowired
  private AchievementsService achievementsService;
  @Autowired
  private ManiaService maniaService;
  @Autowired
  private PlayerService playerService;
  @Autowired
  private AchievementStateStore stateStore;
  @Autowired
  private AchievementRuleStore ruleStore;
  @Autowired
  private AchievementGameIndex gameIndex;
  @Autowired
  private AchievementRarityCache rarityCache;
  @Autowired
  private AchievementUnlockRepository unlockRepository;

  private static final long ACCOUNT_CHECK_TTL_MS = 10 * 60 * 1000;

  private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "Achievements Cloud Sync"));
  private final Map<String, AccountCheck> accountChecks = new ConcurrentHashMap<>();

  @Override
  public void achievementsChanged(@NonNull Set<Long> playerIds) {
    if (!isEnabled()) {
      return;
    }
    executor.submit(() -> {
      for (Long playerId : playerIds) {
        Player player = playerService.getBuildInPlayer(playerId);
        if (player != null) {
          syncPlayer(player, false);
        }
      }
    });
  }

  public boolean isEnabled() {
    return Features.MANIA_ENABLED && achievementsService.getSettings().isCloudSyncEnabled();
  }

  /**
   * True if the player has a VPin Mania account and this cabinet is registered for VPin Mania. The account UUID
   * alone is not enough: it stays in the player record when the cabinet has been deleted outside of Studio or
   * Studio points to another VPin Mania server.
   */
  public boolean isManiaLinked(@NonNull Player player) {
    return Features.MANIA_ENABLED && !StringUtils.isEmpty(player.getManiaAccountUuid()) && getCabinet() != null
        && accountExists(player.getManiaAccountUuid());
  }

  /**
   * Checks that the account still exists in VPin Mania (it may have been deleted there). The result is cached for
   * {@link #ACCOUNT_CHECK_TTL_MS}. If VPin Mania cannot be reached the account is assumed to exist and nothing is
   * cached, so being offline never unlinks players.
   */
  private boolean accountExists(@NonNull String accountUuid) {
    AccountCheck check = accountChecks.get(accountUuid);
    if (check != null && System.currentTimeMillis() - check.checkedAt() < ACCOUNT_CHECK_TTL_MS) {
      return check.exists();
    }
    VPinManiaClient client = client();
    if (client == null) {
      return true;
    }
    try {
      // the search matches the UUID exactly and ignores the visibility of the account
      boolean exists = client.getAccountClient().searchAccounts(accountUuid).stream()
          .anyMatch(a -> accountUuid.equals(a.getUuid()));
      if (!exists) {
        LOG.info("Achievements: VPin Mania account {} does not exist (anymore), the player is handled as local only.", accountUuid);
      }
      accountChecks.put(accountUuid, new AccountCheck(exists, System.currentTimeMillis()));
      return exists;
    }
    catch (Exception e) {
      LOG.warn("Achievements: failed to check VPin Mania account {}: {}", accountUuid, e.getMessage());
      return true;
    }
  }

  private record AccountCheck(boolean exists, long checkedAt) {
  }

  @NonNull
  public AchievementsSyncResult syncAll(boolean refreshRarity) {
    AchievementsSyncResult result = new AchievementsSyncResult();
    if (!isEnabled()) {
      result.message = "VPin Mania synchronization is disabled.";
      return result;
    }
    if (getCabinet() == null) {
      result.message = "This cabinet is not registered for VPin Mania.";
      return result;
    }
    // a synchronization started by the user re-checks the accounts
    accountChecks.clear();
    for (Player player : achievementsService.getTrackedPlayers()) {
      if (!isManiaLinked(player)) {
        result.skippedPlayers++;
        continue;
      }
      int uploaded = syncPlayer(player, true);
      if (uploaded >= 0) {
        result.syncedPlayers++;
        result.uploadedUnlocks += uploaded;
      }
    }
    if (refreshRarity) {
      refreshRarity(true);
    }
    result.success = true;
    result.message = "Synchronized " + result.syncedPlayers + " player(s), " + result.uploadedUnlocks + " new achievement(s) uploaded.";
    return result;
  }

  @Nullable
  private VPinManiaClient client() {
    try {
      return maniaService.getClient();
    }
    catch (Exception e) {
      return null;
    }
  }

  @Nullable
  private Cabinet getCabinet() {
    VPinManiaClient client = client();
    if (client == null) {
      return null;
    }
    try {
      return client.getCabinetClient().getDefaultCabinetCached();
    }
    catch (Exception e) {
      LOG.warn("Achievements: failed to read the VPin Mania cabinet: {}", e.getMessage());
      return null;
    }
  }

  /**
   * @return the number of uploaded unlocks, -1 if the player could not be synchronized
   */
  public synchronized int syncPlayer(@NonNull Player player, boolean force) {
    String accountUuid = player.getManiaAccountUuid();
    VPinManiaClient client = client();
    Cabinet cabinet = getCabinet();
    if (client == null || cabinet == null || !isManiaLinked(player)) {
      return -1;
    }
    try {
      AchievementPlayerState state = stateStore.getState(player.getId());
      if (state.getLastSync() == null || force) {
        restoreFromMania(client, player, accountUuid);
      }

      List<AchievementUnlock> unlocks = unlockRepository.findByPlayerId(player.getId());
      List<AchievementUnlock> pending = unlocks.stream().filter(u -> u.getSyncedAt() == null && u.isUploadable()).toList();
      if (pending.isEmpty() && !state.isSummaryDirty() && !force) {
        return 0;
      }

      AchievementSync sync = buildSync(state, unlocks, pending);
      AchievementSyncResponse response = client.getAchievementClient().sync(cabinet.getId(), accountUuid, sync);
      if (response == null || !response.success) {
        LOG.warn("Achievements: VPin Mania sync for {} failed: {}", player.getName(), response != null ? response.message : "no response");
        return -1;
      }
      Instant now = Instant.now();
      for (AchievementUnlock u : pending) {
        u.setSyncedAt(now);
      }
      unlockRepository.saveAll(pending);

      state = stateStore.getState(player.getId());
      state.setLastSync(now);
      state.setSummaryDirty(false);
      if (!state.isCloudUploadDone()) {
        state.setCloudUploadDone(true);
        // the Cloud Pioneer badge
        if (!achievementsService.updateBadges(state, unlocks).isEmpty()) {
          state.setSummaryDirty(true);
        }
      }
      stateStore.save(state);
      LOG.info("Achievements: synchronized {} with VPin Mania, {} new unlock(s)", player.getName(), pending.size());
      return pending.size();
    }
    catch (Exception e) {
      LOG.warn("Achievements: VPin Mania sync for {} failed: {}", player.getName(), e.getMessage());
      return -1;
    }
  }

  private AchievementSync buildSync(AchievementPlayerState state, List<AchievementUnlock> unlocks, List<AchievementUnlock> pending) {
    AchievementSync sync = new AchievementSync();
    for (AchievementUnlock u : pending) {
      sync.unlocks.add(toMania(u));
    }
    sync.progress = buildProgress(state, unlocks);
    sync.summary = buildSummary(state, unlocks);
    return sync;
  }

  private static de.mephisto.vpin.connectors.mania.model.AchievementUnlock toMania(AchievementUnlock u) {
    de.mephisto.vpin.connectors.mania.model.AchievementUnlock m = new de.mephisto.vpin.connectors.mania.model.AchievementUnlock();
    m.achKey = u.getAchKey();
    m.title = u.getTitle();
    m.rom = u.getRom();
    m.scope = u.getScope();
    m.vpsTableId = u.getVpsTableId();
    m.vpsVersionId = u.getVpsVersionId();
    m.unlockedAt = u.getUnlockedAt() != null ? Date.from(u.getUnlockedAt()) : new Date();
    m.source = u.getSource();
    return m;
  }

  private List<AchievementProgress> buildProgress(AchievementPlayerState state, List<AchievementUnlock> unlocks) {
    Map<String, Set<String>> unlockedByRom = new HashMap<>();
    for (AchievementUnlock u : unlocks) {
      if (u.getRom() != null && u.isUploadable()) {
        unlockedByRom.computeIfAbsent(u.getRom(), r -> new HashSet<>()).add(u.getTitle());
      }
    }
    List<AchievementProgress> result = new ArrayList<>();
    for (Map.Entry<String, Set<String>> e : unlockedByRom.entrySet()) {
      List<AchievementRule> rules = ruleStore.getRomRules(e.getKey());
      if (rules == null || rules.isEmpty()) {
        continue;
      }
      AchievementProgress p = new AchievementProgress();
      p.rom = e.getKey();
      p.total = rules.size();
      p.unlocked = (int) rules.stream().filter(r -> e.getValue().contains(r.getTitleOrDefault())).count();
      p.percentage = Math.round(p.unlocked * 1000d / p.total) / 10d;
      List<Game> games = gameIndex.gamesOfRom(e.getKey());
      if (!games.isEmpty()) {
        Game game = games.get(0);
        p.tableName = game.getGameDisplayName();
        p.vpsTableId = game.getExtTableId();
        p.vpsVersionId = game.getExtTableVersionId();
      }
      result.add(p);
    }
    return result;
  }

  private AchievementSummary buildSummary(AchievementPlayerState state, List<AchievementUnlock> unlocks) {
    AchievementSummary summary = new AchievementSummary();
    summary.uniqueCount = AchievementsService.uniqueTitles(unlocks);
    summary.badges = new ArrayList<>(stateStore.getBadges(state));
    summary.selectedBadge = state.getSelectedBadge();
    return summary;
  }

  /**
   * Adds unlocks that are stored in VPin Mania but not locally (restore after a reinstallation or from another cabinet
   * with the same account).
   */
  private void restoreFromMania(VPinManiaClient client, Player player, String accountUuid) {
    AchievementAccountData data = client.getAchievementClient().getAccountData(accountUuid);
    if (data == null || data.unlocks == null || data.unlocks.isEmpty()) {
      return;
    }
    Set<String> known = new HashSet<>();
    unlockRepository.findByPlayerId(player.getId()).forEach(u -> known.add(u.getTitle()));
    List<AchievementUnlock> restored = new ArrayList<>();
    Instant now = Instant.now();
    for (de.mephisto.vpin.connectors.mania.model.AchievementUnlock m : data.unlocks) {
      if (m.title == null || !known.add(m.title)) {
        continue;
      }
      AchievementUnlock u = new AchievementUnlock();
      u.setPlayerId(player.getId());
      u.setTitle(m.title);
      u.setAchKey(AchievementKeys.key(m.title));
      u.setScope("rom".equals(m.scope) ? "rom" : "global");
      u.setRom(m.rom);
      u.setVpsTableId(m.vpsTableId);
      u.setVpsVersionId(m.vpsVersionId);
      u.setUnlockedAt(m.unlockedAt != null ? m.unlockedAt.toInstant() : now);
      u.setSource(AchievementUnlock.SOURCE_MANIA);
      u.setSyncedAt(now);
      restored.add(u);
    }
    if (!restored.isEmpty()) {
      unlockRepository.saveAll(restored);
      AchievementPlayerState state = stateStore.getState(player.getId());
      if (data.summary != null && data.summary.badges != null) {
        Set<String> badges = stateStore.getBadges(state);
        badges.addAll(data.summary.badges);
        stateStore.setBadges(state, badges);
        if (state.getSelectedBadge() == null) {
          state.setSelectedBadge(data.summary.selectedBadge);
        }
      }
      stateStore.save(state);
      LOG.info("Achievements: restored {} unlock(s) of {} from VPin Mania", restored.size(), player.getName());
    }
  }

  /**
   * Uploads the unlocks of a migration (source "firebase") via the import endpoint, which accepts each Firebase
   * Player ID only once.
   *
   * @return null on success, otherwise an error message
   */
  @Nullable
  public String uploadImport(@NonNull Player player, @NonNull String firebasePid) {
    VPinManiaClient client = client();
    Cabinet cabinet = getCabinet();
    if (client == null || cabinet == null || !isManiaLinked(player)) {
      return "VPin Mania is not available, the data is only imported locally.";
    }
    List<AchievementUnlock> unlocks = unlockRepository.findByPlayerId(player.getId());
    List<AchievementUnlock> imported = unlocks.stream()
        .filter(u -> AchievementUnlock.SOURCE_FIREBASE.equals(u.getSource()) && u.getSyncedAt() == null).toList();
    AchievementPlayerState state = stateStore.getState(player.getId());
    AchievementImportRequest request = new AchievementImportRequest();
    request.firebasePid = firebasePid;
    request.data = buildSync(state, unlocks, imported);
    try {
      AchievementSyncResponse response = client.getAchievementClient().importFirebase(cabinet.getId(), player.getManiaAccountUuid(), request);
      if (response == null || !response.success) {
        markLocalOnly(imported);
        return response != null && response.message != null ? response.message : "The VPin Mania import failed.";
      }
      Instant now = Instant.now();
      imported.forEach(u -> u.setSyncedAt(now));
      unlockRepository.saveAll(imported);
      return null;
    }
    catch (Exception e) {
      // e.g. HTTP 409: the Player ID has already been imported into VPin Mania
      markLocalOnly(imported);
      String msg = e.getMessage() != null && e.getMessage().contains("409")
          ? "This Achievement Watcher Player ID has already been imported into VPin Mania, the data is only imported locally."
          : "The VPin Mania import failed: " + e.getMessage();
      LOG.warn("Achievements: {}", msg);
      return msg;
    }
  }

  /**
   * Imported data that could not be uploaded with the import endpoint must not be uploaded by the regular sync.
   */
  private void markLocalOnly(List<AchievementUnlock> unlocks) {
    unlocks.forEach(u -> u.setSource(AchievementUnlock.SOURCE_LOCAL_IMPORT));
    unlockRepository.saveAll(unlocks);
  }

  /**
   * Fetches the rarity of the ROMs of all tracked players' unlocks.
   */
  public void refreshRarity(boolean force) {
    VPinManiaClient client = client();
    if (client == null || !isEnabled()) {
      return;
    }
    Set<String> roms = new HashSet<>();
    for (Player player : achievementsService.getTrackedPlayers()) {
      for (AchievementUnlock u : unlockRepository.findByPlayerId(player.getId())) {
        if (u.getRom() != null) {
          roms.add(u.getRom());
        }
      }
    }
    for (String rom : roms) {
      if (force || rarityCache.isStale(rom)) {
        AchievementRarity rarity = client.getAchievementClient().getRarity(rom);
        if (rarity != null) {
          rarityCache.put(rom, rarity.players, rarity.counts);
        }
      }
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    achievementsService.addChangeListener(this);
    if (Features.MANIA_ENABLED) {
      executor.submit(() -> {
        try {
          refreshRarity(false);
          syncAll(false);
        }
        catch (Exception e) {
          LOG.warn("Achievements: initial VPin Mania sync failed: {}", e.getMessage());
        }
      });
    }
  }
}
