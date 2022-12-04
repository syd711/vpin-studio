package de.mephisto.vpin.server.popper;

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
  private GameService service;

  @Autowired
  private PinUPConnector connector;

  @Autowired
  private PopperService popperManager;

  @PostMapping("/gameLaunch/")
  public boolean gameLaunch(@RequestParam("table") String table) {
    File tableFile = new File(table);
    Game game = service.getGameByFile(tableFile);
    if (game == null) {
      LOG.warn("No game found for name '" + table);
      return false;
    }
    popperManager.notifyTableStatusChange(game, true);
    return true;
  }

  @PostMapping("/gameExit")
  public boolean gameExit(@RequestParam("table") String table) {
    File tableFile = new File(table.trim());
    Game game = service.getGameByFile(tableFile);
    if (game == null) {
      LOG.warn("No game found for name '" + table);
      return false;
    }
    popperManager.notifyTableStatusChange(game, false);
    return true;
  }

  @PostMapping("/popperLaunch")
  public boolean popperLaunch() {
    popperManager.notifyPopperLaunch();
    return true;
  }

  @PostMapping("/validateScreen/{screen}")
  public String validateScreenConfiguration(@PathVariable("screen") String screenName) {
    PopperScreen screen = PopperScreen.valueOf(screenName);
    PinUPControl fn = null;
    switch (screen) {
      case Other2: {
        fn = connector.getFunction(PinUPControl.FUNCTION_SHOW_OTHER);
        break;
      }
      case GameHelp: {
        fn = connector.getFunction(PinUPControl.FUNCTION_SHOW_HELP);
        break;
      }
      case GameInfo: {
        fn = connector.getFunction(PinUPControl.FUNCTION_SHOW_FLYER);
        break;
      }
      default: {

      }
    }

    if (fn != null) {
      if (!fn.isActive()) {
        return "The screen has not been activated.";
      }
      if (fn.getCtrlKey() == 0) {
        return "The screen is not bound to any key.";
      }
    }
    return null;
  }
}
