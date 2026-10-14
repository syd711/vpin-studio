package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.Rom2AltSoundInfo;
import de.mephisto.vpin.restclient.altsound.Rom2AltSoundOptions;
import de.mephisto.vpin.restclient.altsound.Rom2AltSoundVersion;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.lang.invoke.MethodHandles;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * Generation of ALT sound packs from a game's ROM with rom2altsound.
 */
@RestController
@RequestMapping(API_SEGMENT + "rom2altsound")
public class Rom2AltSoundResource {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private Rom2AltSoundService rom2AltSoundService;

  @Autowired
  private GameService gameService;

  @GetMapping("/available")
  public boolean isAvailable() {
    return rom2AltSoundService.isAvailable();
  }

  @GetMapping("/version")
  public Rom2AltSoundVersion getVersion(@RequestParam(value = "refresh", defaultValue = "false") boolean refresh) {
    return rom2AltSoundService.getVersion(refresh);
  }

  /**
   * Installs or updates rom2altsound with the latest GitHub release.
   */
  @PostMapping("/install")
  public Rom2AltSoundVersion install() {
    try {
      return rom2AltSoundService.install();
    }
    catch (Exception e) {
      LOG.error("rom2altsound installation failed: {}", e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "rom2altsound installation failed: " + e.getMessage());
    }
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
