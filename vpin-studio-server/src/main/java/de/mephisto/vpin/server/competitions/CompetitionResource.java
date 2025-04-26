package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.competitions.IScoredSyncModel;
import de.mephisto.vpin.server.competitions.iscored.IScoredCompetitionSynchronizer;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.players.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "competitions")
public class CompetitionResource {

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private IScoredCompetitionSynchronizer competitionSynchronizer;

  @GetMapping("/offline")
  public List<Competition> getOfflineCompetitions() {
    return competitionService.getOfflineCompetitions();
  }

  @GetMapping("/discord")
  public List<Competition> getDiscordCompetitions() {
    return competitionService.getDiscordCompetitions();
  }

  @GetMapping("/subscriptions")
  public List<Competition> getSubscriptions() {
    return competitionService.getSubscriptions();
  }

  @GetMapping("/iscored")
  public List<Competition> getIScoredSubscriptions() {
    return competitionService.getIScoredSubscriptions();
  }

  @PostMapping("/iscored/synchronize")
  public boolean synchronize(@RequestBody IScoredSyncModel syncModel) {
    return competitionSynchronizer.synchronize(syncModel);
  }

  @GetMapping("/{id}")
  public Competition getCompetition(@PathVariable("id") int id) {
    Competition c = competitionService.getCompetition(id);
    if (c != null) {
      return c;
    }
    throw new ResponseStatusException(NOT_FOUND, "Not competition found for id " + id);
  }

  @GetMapping("/game/{id}")
  public List<Competition> existsForGame(@PathVariable("id") int id) {
    return competitionService.getCompetitionForGame(id);
  }

  @GetMapping("/competition/{uuid}")
  public Competition byUuid(@PathVariable("uuid") String uuid) {
    return competitionService.getCompetitionForUuid(uuid);
  }

  @GetMapping("/finished/{limit}")
  public List<Competition> getFinishedCompetitions(@PathVariable("limit") int limit) {
    return competitionService.getFinishedCompetitions(limit);
  }

  @GetMapping("/players/{id}")
  public List<Player> getCompetitionPlayers(@PathVariable("id") long id) {
    return competitionService.getDiscordCompetitionPlayers(id);
  }

  @GetMapping("/{type}/active")
  public Competition getActiveCompetition(@PathVariable("type") String type) {
    CompetitionType ct = CompetitionType.valueOf(type);
    return competitionService.getActiveCompetition(ct);
  }

  @GetMapping("/scores/{id}")
  public ScoreList getScores(@PathVariable("id") int id) {
    return competitionService.getCompetitionScores(id);
  }

  @GetMapping("/score/{id}")
  public ScoreSummary getScoreSummary(@PathVariable("id") int id) {
    return competitionService.getCompetitionScore(id);
  }

  @PutMapping("/finish/{id}")
  public boolean finishCompetition(@PathVariable("id") int id) {
    return competitionService.finishCompetition(id) != null;
  }

  @PostMapping("/save")
  public Competition save(@RequestBody Competition c) {
    Competition save = competitionService.save(c);
    competitionService.runCompetitionsFinishedAndStartedCheck();
    return save;
  }

  @DeleteMapping("/{id}")
  public void deleteCompetition(@PathVariable("id") int id) {
    competitionService.delete(id);
  }

}
