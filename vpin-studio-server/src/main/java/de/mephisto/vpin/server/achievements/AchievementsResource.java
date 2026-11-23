package de.mephisto.vpin.server.achievements;

import de.mephisto.vpin.restclient.achievements.AchievementsMigrationRequest;
import de.mephisto.vpin.restclient.achievements.AchievementsMigrationResult;
import de.mephisto.vpin.restclient.achievements.AchievementsSyncResult;
import de.mephisto.vpin.restclient.achievements.PlayerAchievements;
import de.mephisto.vpin.restclient.achievements.TableAchievements;
import de.mephisto.vpin.restclient.achievements.TableAchievementsDetails;
import de.mephisto.vpin.server.achievements.cloud.AchievementsCloudService;
import de.mephisto.vpin.server.achievements.migration.AchievementsMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "achievements")
public class AchievementsResource {

  @Autowired
  private AchievementsQueryService queryService;

  @Autowired
  private AchievementsService achievementsService;

  @Autowired
  private AchievementsCloudService cloudService;

  @Autowired
  private AchievementsMigrationService migrationService;

  @GetMapping("/players")
  public List<PlayerAchievements> getPlayers() {
    return queryService.getPlayers();
  }

  @GetMapping("/player/{playerId}")
  public PlayerAchievements getPlayer(@PathVariable("playerId") long playerId) {
    PlayerAchievements p = queryService.getPlayerAchievements(playerId);
    if (p == null) {
      throw new ResponseStatusException(NOT_FOUND, "No player found for id " + playerId);
    }
    return p;
  }

  @GetMapping("/player/{playerId}/tables")
  public List<TableAchievements> getTables(@PathVariable("playerId") long playerId) {
    return queryService.getTables(playerId);
  }

  @GetMapping("/player/{playerId}/table/{rom}")
  public TableAchievementsDetails getTable(@PathVariable("playerId") long playerId, @PathVariable("rom") String rom) {
    return queryService.getTableDetails(playerId, rom);
  }

  @PutMapping("/player/{playerId}/badge")
  public boolean selectBadge(@PathVariable("playerId") long playerId, @RequestParam(value = "badge", required = false) String badge) {
    return achievementsService.selectBadge(playerId, badge);
  }

  @PostMapping("/sync")
  public AchievementsSyncResult sync() {
    return cloudService.syncAll(true);
  }

  @PostMapping("/migration")
  public AchievementsMigrationResult migrate(@RequestBody AchievementsMigrationRequest request) {
    return migrationService.migrate(request);
  }

  @GetMapping("/migration/watcherfolder")
  public String getDefaultWatcherFolder() {
    return migrationService.findDefaultWatcherFolder();
  }
}
