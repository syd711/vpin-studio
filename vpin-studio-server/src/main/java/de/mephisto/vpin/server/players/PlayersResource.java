package de.mephisto.vpin.server.players;

import de.mephisto.vpin.restclient.PlayerDomain;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.ScoreList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "players")
public class PlayersResource {

  @Autowired
  private PlayerService playerService;

  @Autowired
  private HighscoreService highscoreService;

  @GetMapping
  public List<Player> getPlayers() {
    return playerService.getBuildInPlayers();
  }

  @GetMapping("/invalidate/{domain}")
  public boolean invalidateDomain(@PathVariable("domain") String domain) {
    return playerService.invalidateDomain(PlayerDomain.valueOf(domain));
  }

  @GetMapping("/domain/{domain}")
  public List<Player> getPlayerForDomain(@PathVariable("domain") String domain) {
    return playerService.getPlayersForDomain(PlayerDomain.valueOf(domain));
  }

  @GetMapping("/highscores/{initials}")
  public ScoreSummary getHighscores(@PathVariable("initials") String initials) {
    return highscoreService.getHighscores(initials);
  }

  @GetMapping("/{id}")
  public Player getCompetition(@PathVariable("id") int id) {
    Player p = playerService.getBuildInPlayer(id);
    if (p == null) {
      throw new ResponseStatusException(NOT_FOUND, "Not player found for id " + id);
    }
    return p;
  }

  @PostMapping("/save")
  public Player save(@RequestBody Player c) {
    return playerService.save(c);
  }

  @DeleteMapping("/delete/{id}")
  public void deletePlayer(@PathVariable("id") int id) {
    playerService.delete(id);
  }

}
