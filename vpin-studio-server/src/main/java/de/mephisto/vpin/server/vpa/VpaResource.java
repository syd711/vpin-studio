package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "vpa")
public class VpaResource {
  private final static Logger LOG = LoggerFactory.getLogger(VpaResource.class);

  @Autowired
  private VpaService vpaService;

  @Autowired
  private GameService gameService;

  @PostMapping("/export/{id}")
  public Boolean export(@PathVariable("id") int id, @RequestBody Map<String, Object> values) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return vpaService.export(game);
    }
    return false;
  }

}
