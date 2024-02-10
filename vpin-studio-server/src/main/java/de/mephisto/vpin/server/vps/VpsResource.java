package de.mephisto.vpin.server.vps;

import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "vps")
public class VpsResource {
  private final static Logger LOG = LoggerFactory.getLogger(VpsResource.class);

  @Autowired
  private VpsService vpsService;

  @Autowired
  private GameService gameService;

  @GetMapping("/automatch/{gameId}/{overwrite}")
  public TableDetails autoMatch(@PathVariable("gameId") int gameId, @PathVariable("overwrite") boolean overwrite) {
    return vpsService.autoMatch(gameService.getGame(gameId), overwrite);
  }
}
