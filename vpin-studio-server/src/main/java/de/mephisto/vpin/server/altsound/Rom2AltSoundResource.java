package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.Rom2AltSoundInfo;
import de.mephisto.vpin.restclient.altsound.Rom2AltSoundOptions;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Generation of ALT sound packs from a game's ROM with rom2altsound.
 */
@RestController
@RequestMapping(API_SEGMENT + "rom2altsound")
public class Rom2AltSoundResource {

  @Autowired
  private Rom2AltSoundService rom2AltSoundService;

  @Autowired
  private GameService gameService;

  @GetMapping("/available")
  public boolean isAvailable() {
    return rom2AltSoundService.isAvailable();
  }

  @GetMapping("/{gameId}")
  public Rom2AltSoundInfo getInfo(@PathVariable("gameId") int gameId) {
    return rom2AltSoundService.getInfo(getGame(gameId));
  }

  @PostMapping("/{gameId}")
  public JobDescriptor generate(@PathVariable("gameId") int gameId, @RequestBody(required = false) Rom2AltSoundOptions options) {
    return rom2AltSoundService.generate(getGame(gameId), options != null ? options : new Rom2AltSoundOptions());
  }

  private Game getGame(int gameId) {
    Game game = gameService.getGame(gameId);
    if (game == null) {
      throw new ResponseStatusException(NOT_FOUND, "Game not found: " + gameId);
    }
    return game;
  }
}
