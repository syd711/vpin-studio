package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.AltSound;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "altsound")
public class AltSoundResource {

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private GameService gameService;

  @GetMapping("{id}")
  public AltSound csv(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return altSoundService.getAltSound(game);
    }
    return new AltSound();
  }

  @PostMapping("/save/{id}")
  public AltSound save(@PathVariable("id") int id, @RequestBody AltSound altSound) throws Exception {
    Game game = gameService.getGame(id);
    if (game != null) {
      return altSoundService.save(game, altSound);
    }
    return new AltSound();
  }

  @GetMapping("/restore/{id}")
  public AltSound restore(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return altSoundService.restore(game);
    }
    return new AltSound();
  }

  @GetMapping("/enabled/{id}")
  public boolean enable(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return altSoundService.isAltSoundEnabled(game);
    }
    return false;
  }

  @GetMapping("/set/{id}/{enable}")
  public boolean enable(@PathVariable("id") int id,
                        @PathVariable("enable") boolean enable) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return altSoundService.setAltSoundEnabled(game, enable);
    }
    return false;
  }
}
