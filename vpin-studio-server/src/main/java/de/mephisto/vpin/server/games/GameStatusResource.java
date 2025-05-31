package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.GameStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "gamestatus")
public class GameStatusResource {
  private final static Logger LOG = LoggerFactory.getLogger(GameStatusResource.class);

  @Autowired
  private GameStatusService gameStatusService;

  @GetMapping
  public GameStatus getStatus() {
    return gameStatusService.getStatus();
  }

  @GetMapping("paused")
  public GameStatus startPause() {
    gameStatusService.getStatus().startPause();
    return gameStatusService.getStatus();
  }

  @GetMapping("unpaused")
  public GameStatus endPause() {
    gameStatusService.getStatus().finishPause();
    return gameStatusService.getStatus();
  }
}
