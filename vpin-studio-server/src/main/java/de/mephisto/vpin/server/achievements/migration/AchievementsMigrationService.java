package de.mephisto.vpin.server.achievements.migration;

import de.mephisto.vpin.restclient.achievements.AchievementsMigrationRequest;
import de.mephisto.vpin.restclient.achievements.AchievementsMigrationResult;
import de.mephisto.vpin.server.achievements.AchievementKeys;
import de.mephisto.vpin.server.achievements.AchievementRuleStore;
import de.mephisto.vpin.server.achievements.AchievementStateStore;
import de.mephisto.vpin.server.achievements.AchievementsService;
import de.mephisto.vpin.server.achievements.cloud.AchievementsCloudService;
import de.mephisto.vpin.server.achievements.model.AchievementPlayerState;
import de.mephisto.vpin.server.achievements.model.AchievementUnlock;
import de.mephisto.vpin.server.achievements.model.AchievementUnlockRepository;
import de.mephisto.vpin.server.achievements.progression.LevelCalculator;
import de.mephisto.vpin.server.achievements.rules.PlayerProgress;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * One-time migration of VPX Achievement Watcher data (plan §7, decisions §10.6/§10.7):
 * <ul>
 *   <li>Firebase: Player ID + recovery key, "claim once, then read". Nothing else is written to Firebase.</li>
 *   <li>Local installation: imported locally; uploaded to VPin Mania only if the claim with the keys of its
 *   cloud_identity.json succeeds, otherwise the unlocks are marked "local-import" and never uploaded.</li>
 * </ul>
 */
@Service
public class AchievementsMigrationService {
  private static final Logger LOG = LoggerFactory.getLogger(AchievementsMigrationService.class);

  private static final String DEFAULT_WATCHER_FOLDER = "C:\\vPinball\\VPX Achievement Watcher";

  @Autowired
  private PlayerService playerService;
  @Autowired
  private AchievementsService achievementsService;
  @Autowired
  private AchievementStateStore stateStore;
  @Autowired
  private AchievementRuleStore ruleStore;
  @Autowired
  private AchievementUnlockRepository unlockRepository;
  @Autowired
  private AchievementsCloudService cloudService;

  /**
   * Factory, replaceable in tests.
   */
  protected FirebaseLegacyClient createFirebaseClient() {
    return new FirebaseLegacyClient();
  }

  @Nullable
  public String findDefaultWatcherFolder() {
    for (String candidate : List.of(DEFAULT_WATCHER_FOLDER, "C:\\VPX Achievement Watcher",
        System.getProperty("user.home") + "\\VPX Achievement Watcher")) {
      File folder = new File(candidate);
      if (new File(folder, "Achievements/achievements_state.json").exists() || new File(folder, "achievements_state.json").exists()) {
        return folder.getAbsolutePath();
      }
    }
    return null;
  }

  @NonNull
  public synchronized AchievementsMigrationResult migrate(@NonNull AchievementsMigrationRequest request) {
    AchievementsMigrationResult result = new AchievementsMigrationResult();
    try {
      Player player = playerService.getBuildInPlayer(request.playerId);
      if (player == null) {
        return fail(result, "Player not found.");
      }
      AchievementPlayerState state = stateStore.getState(player.getId());
      if (!request.previewOnly && state.getMigratedFirebasePid() != null
          && AchievementsMigrationRequest.SOURCE_FIREBASE.equals(request.source)) {
        return fail(result, "The Achievement Watcher Player ID \"" + state.getMigratedFirebasePid() + "\" has already been migrated for this player.");
      }

      FirebaseLegacyClient firebase = createFirebaseClient();
      WatcherData data;
      if (AchievementsMigrationRequest.SOURCE_LOCAL.equals(request.source)) {
        if (StringUtils.isEmpty(request.watcherFolder) || !new File(request.watcherFolder).exists()) {
          return fail(result, "The Achievement Watcher folder does not exist.");
        }
        data = new LocalWatcherImporter().read(new File(request.watcherFolder));
        if (data == null) {
          return fail(result, "No achievements_state.json found in " + request.watcherFolder + ".");
        }
        if (!StringUtils.isEmpty(request.firebasePid)) {
          data.pid = request.firebasePid.trim().toLowerCase();
        }
        if (!StringUtils.isEmpty(request.recoveryKey)) {
          data.recoveryKey = request.recoveryKey.trim();
        }
        result.signatureValid = data.signatureValid;
        if (!data.signatureValid) {
          result.warnings.add("The signature of the local achievements file is not valid (the file has been modified outside the Watcher).");
        }
      }
      else {
        String pid = StringUtils.trimToEmpty(request.firebasePid).toLowerCase();
        if (!pid.matches("[a-z0-9]{4}")) {
          return fail(result, "The Player ID must have 4 characters (letters and digits).");
        }
        data = readFirebase(firebase, pid);
        if (data == null) {
          return fail(result, "No Achievement Watcher cloud data found for Player ID \"" + pid + "\".");
        }
        data.pid = pid;
        data.recoveryKey = StringUtils.trimToNull(request.recoveryKey);
      }

      fillPreview(result, data);
      boolean maniaLinked = !StringUtils.isEmpty(player.getManiaAccountUuid());
      if (request.previewOnly) {
        result.cloudUpload = maniaLinked && data.pid != null && data.recoveryKey != null;
        result.success = true;
        result.message = "Preview";
        return result;
      }

      // ownership proof: the claim is the only write to Firebase
      boolean verified = false;
      if (data.pid != null && data.recoveryKey != null) {
        if (data.recoveryKey.length() < 24 || data.recoveryKey.length() > 64) {
          result.warnings.add("The recovery key has an invalid length.");
        }
        else {
          FirebaseLegacyClient.ClaimResult claim = firebase.claim(data.pid, data.recoveryKey);
          verified = claim == FirebaseLegacyClient.ClaimResult.OK;
          if (claim == FirebaseLegacyClient.ClaimResult.WRONG_KEY) {
            result.warnings.add("The recovery key does not match the Player ID.");
          }
          else if (claim == FirebaseLegacyClient.ClaimResult.OFFLINE) {
            result.warnings.add("The Achievement Watcher cloud could not be reached.");
          }
        }
      }
      if (AchievementsMigrationRequest.SOURCE_FIREBASE.equals(request.source) && !verified) {
        return fail(result, "The ownership of the Player ID could not be verified. "
            + (result.warnings.isEmpty() ? "Please check the recovery key." : String.join(" ", result.warnings)));
      }
      result.cloudVerified = verified;
      result.cloudUpload = verified && maniaLinked;
      if (verified && !maniaLinked) {
        result.warnings.add("The player is not linked to a VPin Mania account, the data is only imported locally.");
      }

      importLocally(player, state, data, verified, result);

      if (result.cloudUpload) {
        String error = cloudService.uploadImport(player, data.pid);
        if (error != null) {
          result.cloudUpload = false;
          result.warnings.add(error);
        }
      }
      result.success = true;
      result.message = "Imported " + result.importedUnlocks + " achievement(s)"
          + (result.cloudUpload ? " and uploaded them to VPin Mania." : " locally.");
      LOG.info("Achievements migration for player {}: {}", player.getName(), result.message);
      return result;
    }
    catch (Exception e) {
      LOG.error("Achievements migration failed: {}", e.getMessage(), e);
      return fail(result, "Migration failed: " + e.getMessage());
    }
  }

  @Nullable
  private WatcherData readFirebase(FirebaseLegacyClient firebase, String pid) {
    JsonNode achievements = firebase.read("players/" + pid + "/achievements");
    if (achievements == null || !achievements.isObject()) {
      return null;
    }
    WatcherData data = new WatcherData();
    data.parse(achievements);
    data.parseSession(firebase.read("players/" + pid + "/achievements/session"));
    JsonNode progress = firebase.read("players/" + pid + "/progress");
    if (progress != null && progress.isObject()) {
      for (Map.Entry<String, JsonNode> e : progress.properties()) {
        data.romsPlayed.add(e.getKey().toLowerCase());
      }
    }
    return data;
  }

  private void fillPreview(AchievementsMigrationResult result, WatcherData data) {
    result.firebasePid = data.pid;
    result.watcherPlayerName = data.playerName;
    result.uniqueAchievements = data.uniqueCount();
    result.romsPlayed = data.romsPlayed.size();
    result.badges = new ArrayList<>(data.badges);
    result.selectedBadge = data.selectedBadge;
    var level = LevelCalculator.compute(result.uniqueAchievements);
    result.level = level.level;
    result.levelName = level.name;
    result.prestige = level.prestige;
  }

  private void importLocally(Player player, AchievementPlayerState state, WatcherData data, boolean verified, AchievementsMigrationResult result) {
    List<AchievementUnlock> existing = unlockRepository.findByPlayerId(player.getId());
    Set<String> known = new HashSet<>();
    existing.forEach(u -> known.add(u.getTitle()));

    List<AchievementUnlock> created = new ArrayList<>();
    for (WatcherData.Entry e : data.entries) {
      if (!known.add(e.title())) {
        result.skippedUnlocks++;
        continue;
      }
      AchievementUnlock u = new AchievementUnlock();
      u.setPlayerId(player.getId());
      u.setTitle(e.title());
      u.setAchKey(AchievementKeys.key(e.title()));
      String rom = e.rom() != null ? e.rom() : null;
      u.setScope(rom != null ? "rom" : "global");
      u.setRom(rom);
      u.setVpsTableId(e.vpsId());
      u.setUnlockedAt(e.ts() != null ? e.ts() : Instant.now());
      u.setSource(verified ? AchievementUnlock.SOURCE_FIREBASE : AchievementUnlock.SOURCE_LOCAL_IMPORT);
      created.add(u);
    }
    unlockRepository.saveAll(created);
    result.importedUnlocks = created.size();

    Set<String> badges = stateStore.getBadges(state);
    badges.addAll(data.badges);
    stateStore.setBadges(state, badges);
    if (state.getSelectedBadge() == null && data.selectedBadge != null && badges.contains(data.selectedBadge)) {
      state.setSelectedBadge(data.selectedBadge);
    }
    Set<String> roms = new LinkedHashSet<>(stateStore.getRomsPlayed(state));
    roms.addAll(data.romsPlayed);
    for (WatcherData.Entry e : data.entries) {
      if (e.rom() != null) {
        roms.add(e.rom());
      }
    }
    PlayerProgress progress = new PlayerProgress();
    progress.romsPlayed = roms;
    progress.globalTally = stateStore.getTally(state);
    for (Map.Entry<String, Long> t : data.tally.entrySet()) {
      PlayerProgress.Tally tally = progress.globalTally.computeIfAbsent(t.getKey(), k -> new PlayerProgress.Tally());
      tally.progress = Math.max(tally.progress, t.getValue());
    }
    stateStore.applyProgress(state, progress);
    if (data.cloudUploadDone) {
      state.setCloudUploadDone(true);
    }
    if (verified) {
      state.setMigratedFirebasePid(data.pid);
    }
    state.setMigratedAt(Instant.now());
    state.setSummaryDirty(true);

    int ruleFiles = 0;
    for (File ruleFile : data.ruleFiles) {
      String rom = ruleFile.getName().substring(0, ruleFile.getName().length() - ".ach.json".length());
      if (ruleStore.importRomRules(rom, ruleFile)) {
        ruleFiles++;
      }
    }
    if (ruleFiles > 0) {
      LOG.info("Imported {} Achievement Watcher rule files", ruleFiles);
    }

    List<AchievementUnlock> all = new ArrayList<>(existing);
    all.addAll(created);
    achievementsService.updateBadges(state, all);
    stateStore.save(state);
  }

  private static AchievementsMigrationResult fail(AchievementsMigrationResult result, String message) {
    result.success = false;
    result.message = message;
    return result;
  }
}
