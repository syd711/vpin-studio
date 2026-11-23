package de.mephisto.vpin.server.achievements;

import de.mephisto.vpin.restclient.achievements.AchievementInfo;
import de.mephisto.vpin.restclient.achievements.AchievementsMigrationRequest;
import de.mephisto.vpin.restclient.achievements.AchievementsMigrationResult;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.restclient.achievements.TableAchievements;
import de.mephisto.vpin.restclient.achievements.TableAchievementsDetails;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.achievements.migration.AchievementsMigrationService;
import de.mephisto.vpin.server.achievements.model.AchievementUnlock;
import de.mephisto.vpin.server.achievements.model.AchievementUnlockRepository;
import de.mephisto.vpin.server.achievements.nvram.NvramSnapshot;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AchievementsServiceIntegrationTest extends AbstractVPinServerTest {

  @Autowired
  private AchievementsService achievementsService;
  @Autowired
  private AchievementsQueryService queryService;
  @Autowired
  private AchievementsMigrationService migrationService;
  @Autowired
  private AchievementRuleStore ruleStore;
  @Autowired
  private AchievementUnlockRepository unlockRepository;
  @Autowired
  private PlayerService playerService;
  @Autowired
  private FolderLookupService folderLookupService;
  @Autowired
  private AchievementGameIndex gameIndex;

  @BeforeAll
  public void setup() {
    setupSystem();
    gameIndex.invalidate();
  }

  private Player createPlayer(String name, String initials, boolean tracked) {
    Player p = new Player();
    p.setName(name);
    p.setInitials(initials);
    p.setAchievementsEnabled(tracked);
    return playerService.save(p);
  }

  @Test
  public void evaluatesGameForTrackedPlayers() throws Exception {
    Player tracked = createPlayer("Tracked", "TRK", true);
    Player untracked = createPlayer("Untracked", "UNT", false);
    try {
      Game game = gameService.getGameByFilename(1, NVRAM_TABLE_NAME);
      assertThat(game).isNotNull();
      String rom = game.getRom().toLowerCase();
      File nv = folderLookupService.getNvRamFile(game);
      NvramSnapshot start = ruleStore.read(rom, nv);
      assertThat(start.audits()).isNotEmpty();

      Map<String, Object> endAudits = new LinkedHashMap<>(start.audits());
      endAudits.replaceAll((k, v) -> v instanceof Long l && !k.startsWith("P1") ? l + 30 : v);
      NvramSnapshot end = new NvramSnapshot(endAudits, 1);

      List<Long> changed = achievementsService.evaluateGame(game, rom, start, end, Instant.now().minusSeconds(700), Instant.now(), 700);
      assertThat(changed).containsExactly(tracked.getId());

      List<AchievementUnlock> unlocks = unlockRepository.findByPlayerId(tracked.getId());
      assertThat(unlocks).isNotEmpty();
      assertThat(unlocks).anyMatch(u -> u.getTitle().equals(rom + " – Play 10 Minutes"));
      assertThat(unlockRepository.findByPlayerId(untracked.getId())).isEmpty();

      PlayerAchievements overview = queryService.getPlayerAchievements(tracked.getId());
      assertThat(overview.unlockedCount).isEqualTo(AchievementsService.uniqueTitles(unlocks));
      assertThat(overview.unlockedThisWeek).isEqualTo(overview.unlockedCount);
      assertThat(overview.recentUnlocks).isNotEmpty();
      assertThat(overview.badges).hasSize(31);
      assertThat(overview.badges).anyMatch(b -> b.id.equals("first_steps") && b.earned);

      List<TableAchievements> tables = queryService.getTables(tracked.getId());
      TableAchievements table = tables.stream().filter(t -> rom.equals(t.rom)).findFirst().orElseThrow();
      assertThat(table.hasMap).isTrue();
      assertThat(table.total).isGreaterThan(0);
      assertThat(table.unlocked).isGreaterThan(0);
      assertThat(table.status).isIn(TableAchievements.STATUS_IN_PROGRESS, TableAchievements.STATUS_COMPLETED);
      assertThat(table.lastPlayed).isNotNull();

      TableAchievementsDetails details = queryService.getTableDetails(tracked.getId(), rom);
      assertThat(details.achievements).hasSize(table.total);
      assertThat(details.achievements.stream().filter(a -> a.unlocked).count()).isEqualTo(table.unlocked);

      TableAchievementsDetails global = queryService.getTableDetails(tracked.getId(), AchievementsQueryService.GLOBAL);
      assertThat(global.achievements).hasSize(ruleStore.getGlobalRules().size());

      // a second identical game does not unlock anything new
      achievementsService.evaluateGame(game, rom, start, end, Instant.now().minusSeconds(700), Instant.now(), 700);
      assertThat(unlockRepository.findByPlayerId(tracked.getId())).hasSameSizeAs(unlocks);

      // multiplayer games skip the per ROM achievements
      Player second = createPlayer("Second", "SEC", true);
      try {
        achievementsService.evaluateGame(game, rom, start, new NvramSnapshot(endAudits, 2), Instant.now().minusSeconds(700), Instant.now(), 700);
        assertThat(unlockRepository.findByPlayerId(second.getId())).noneMatch(u -> "rom".equals(u.getScope()));
      }
      finally {
        playerService.delete(second.getId());
      }
    }
    finally {
      playerService.delete(tracked.getId());
      playerService.delete(untracked.getId());
    }
    assertThat(unlockRepository.findByPlayerId(tracked.getId())).isEmpty();
  }

  @Test
  public void migratesLocalInstallationLocallyOnly(@TempDir Path tmp) throws Exception {
    Player player = createPlayer("Migrated", "MIG", true);
    try {
      // without cloud_identity.json there is no ownership proof: local import only, no network access
      File install = tmp.toFile();
      FileUtils.copyDirectory(new File(LocalWatcherImporterTest.INSTALL, "Achievements"), new File(install, "Achievements"));

      AchievementsMigrationRequest request = new AchievementsMigrationRequest();
      request.playerId = player.getId();
      request.source = AchievementsMigrationRequest.SOURCE_LOCAL;
      request.watcherFolder = install.getAbsolutePath();
      request.previewOnly = true;
      AchievementsMigrationResult preview = migrationService.migrate(request);
      assertThat(preview.success).isTrue();
      assertThat(preview.uniqueAchievements).isEqualTo(5);
      assertThat(preview.signatureValid).isTrue();
      assertThat(unlockRepository.findByPlayerId(player.getId())).isEmpty();

      request.previewOnly = false;
      AchievementsMigrationResult result = migrationService.migrate(request);
      assertThat(result.success).as(result.message).isTrue();
      assertThat(result.cloudVerified).isFalse();
      assertThat(result.cloudUpload).isFalse();
      assertThat(result.importedUnlocks).isEqualTo(5);
      assertThat(unlockRepository.findByPlayerId(player.getId()))
          .allMatch(u -> AchievementUnlock.SOURCE_LOCAL_IMPORT.equals(u.getSource()) && !u.isUploadable());

      PlayerAchievements overview = queryService.getPlayerAchievements(player.getId());
      assertThat(overview.selectedBadge).isEqualTo("night_owl");
      assertThat(overview.badges).anyMatch(b -> b.id.equals("night_owl") && b.earned);

      // importing again skips the existing unlocks
      AchievementsMigrationResult again = migrationService.migrate(request);
      assertThat(again.importedUnlocks).isZero();
      assertThat(again.skippedUnlocks).isEqualTo(5);

      // the imported Watcher rule file is used for the table (afm_113b is not installed in the test system)
      assertThat(ruleStore.getRomRules("afm_113b")).isNotNull();
      List<AchievementInfo> afm = queryService.getTableDetails(player.getId(), "afm_113b").achievements;
      AchievementInfo play5 = afm.stream().filter(a -> a.title.equals("afm_113b – Play 5 Minutes")).findFirst().orElseThrow();
      assertThat(play5.unlocked).isTrue();
      assertThat(play5.legacy).isFalse();
      // "Ramps Made" is not a field of the AFM map (AFM uses upper case labels): kept as legacy unlock
      AchievementInfo ramps = afm.stream().filter(a -> a.title.equals("afm_113b – Ramps Made: 5")).findFirst().orElseThrow();
      assertThat(ramps.unlocked).isTrue();
      assertThat(ramps.legacy).isTrue();
    }
    finally {
      playerService.delete(player.getId());
      FileUtils.deleteQuietly(ruleStore.getRomRuleFile("afm_113b"));
    }
  }
}
