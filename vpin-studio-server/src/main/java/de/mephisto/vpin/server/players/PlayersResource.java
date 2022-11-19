package de.mephisto.vpin.server.players;

import de.mephisto.vpin.connectors.discord.DiscordMember;
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
  private DiscordPlayerService discordPlayerService;

  @GetMapping
  public List<Player> getPlayers() {
    return playerService.getBuildInPlayers();
  }

  @GetMapping("/discord")
  public List<DiscordMember> getDiscordPlayers() {
    return discordPlayerService.getMembers();
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
