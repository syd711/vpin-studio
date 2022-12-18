package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.restclient.PinUPControl;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;

/**
 * Legacy URls:
 * "curl -X POST --data-urlencode \"info=\" http://localhost:" + HttpServer.PORT + "/service/popperLaunch";
 * "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + HttpServer.PORT + "/service/gameLaunch";
 * "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + HttpServer.PORT + "/service/gameExit";
 */
@RestController
@RequestMapping("/service") //do not add api version
public class PopperResource {
  private final static Logger LOG = LoggerFactory.getLogger(PopperResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private PopperService popperService;

  @PostMapping("/gameLaunch/")
  public boolean gameLaunch(@RequestParam("table") String table) {
    File tableFile = new File(table);
    Game game = gameService.getGameByFilename(tableFile.getName());

    new Thread(() -> {
      if (game == null) {
        LOG.warn("No game found for name '" + table);
      }
      else {
        Thread.currentThread().setName("Popper Game Launch Thread");
        popperService.notifyTableStatusChange(game, true);
      }
    }).start();
    return game != null;
  }

  @PostMapping("/gameExit")
  public boolean gameExit(@RequestParam("table") String table) {
    File tableFile = new File(table.trim());
    Game game = gameService.getGameByFilename(tableFile.getName());

    new Thread(() -> {
      if (game == null) {
        LOG.warn("No game found for name '" + table);
      }
      else {
        Thread.currentThread().setName("Popper Game Exit Thread");
        popperService.notifyTableStatusChange(game, false);
      }
    }).start();
    return game != null;
  }

  @PostMapping("/popperLaunch")
  public boolean popperLaunch() {
    popperService.notifyPopperLaunch();
    return true;
  }

  @GetMapping("/pincontrol/{screen}")
  public PinUPControl getPinUPControlFor(@PathVariable("screen") String screenName) {
    return popperService.getPinUPControlFor(PopperScreen.valueOf(screenName));
  }
}
