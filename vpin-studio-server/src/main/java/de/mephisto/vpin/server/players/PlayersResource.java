package de.mephisto.vpin.server.players;

import de.mephisto.vpin.restclient.players.PlayerDomain;
import de.mephisto.vpin.server.competitions.RankedPlayer;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.highscores.HighscoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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

  @GetMapping("/domain/{domain}")
  public List<Player> getPlayerForDomain(@PathVariable("domain") String domain) {
    return playerService.getPlayersForDomain(PlayerDomain.valueOf(domain));
  }

  @GetMapping("/highscores/{initials}")
  public ScoreSummary getHighscores(@PathVariable("initials") String initials) {
    initials = URLDecoder.decode(initials, StandardCharsets.UTF_8);
    return highscoreService.getAllHighscoresForPlayer(initials);
  }

  @GetMapping("/{id}")
  public Player getPlayerById(@PathVariable("id") int id) {
    Player p = playerService.getBuildInPlayer(id);
    if (p == null) {
      throw new ResponseStatusException(NOT_FOUND, "No player found for id " + id);
    }
    return p;
  }

  @GetMapping("/player/{serverId}/{initials}")
  public Player getPlayerById(@PathVariable("serverId") long serverId, @PathVariable("initials") String initials) {
    Player player = playerService.getPlayerForInitials(serverId, initials);
    if (player == null) {
      throw new ResponseStatusException(NOT_FOUND, "No player found for initials '" + initials + "'");
    }
    return player;
  }

  @PostMapping("/save")
  public Player save(@RequestBody Player player) {
    return playerService.save(player);
  }

  @DeleteMapping("/{id}")
  public void deletePlayer(@PathVariable("id") int id) {
    playerService.delete(id);
  }

  @GetMapping("/ranked")
  public List<RankedPlayer> getPlayerByRanks() {
    return highscoreService.getPlayersByRanks();
  }
}
