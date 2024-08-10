package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameStatusService;
import de.mephisto.vpin.server.games.TableStatusChangedOrigin;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * Legacy URls:
 * "curl -X POST --data-urlencode \"info=\" http://localhost:" + HttpServer.PORT + "/service/popperLaunch";
 * "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + HttpServer.PORT + "/service/gameLaunch";
 * "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + HttpServer.PORT + "/service/gameExit";
 */
@RestController
@RequestMapping("/service")
//do not add api version AND DO NOT CHANGE "service" segment (these are already stored in popper too)
public class FrontendStatusEventsResource {
  private final static Logger LOG = LoggerFactory.getLogger(FrontendStatusEventsResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private GameStatusService gameStatusService;

  @PostMapping("/gameLaunch")
  public boolean gameLaunch(@RequestParam("table") String table) {
    LOG.info("Received game launch event for " + table.trim());
    Game game = resolveGame(table);
    if (game == null) {
      LOG.warn("No game found for name '" + table);
      return false;
    }

    if (gameStatusService.getStatus().getGameId() == game.getId()) {
      LOG.info("Skipped launch event, since the game has been marked as active already.");
      return false;
    }

    new Thread(() -> {
      Thread.currentThread().setName("Game Launch Thread");
      frontendStatusService.notifyTableStatusChange(game, true, TableStatusChangedOrigin.ORIGIN_POPPER);
    }).start();
    return game != null;
  }

  @PostMapping("/gameExit")
  public boolean gameExit(@RequestParam("table") String table) {
    LOG.info("Received game exit event for " + table.trim());
    Game game = resolveGame(table);
    new Thread(() -> {
      Thread.currentThread().setName("Game Exit Thread");
      if (game == null) {
        LOG.warn("No game found for name '" + table);
        return;
      }

      SLOG.initLog(game.getId());
      if (!gameStatusService.getStatus().isActive()) {
        LOG.info("Skipped exit event, since the no game is currently running.");
        return;
      }

      frontendStatusService.notifyTableStatusChange(game, false, TableStatusChangedOrigin.ORIGIN_POPPER);
      SLOG.finalizeEventLog();
    }).start();
    return game != null;
  }

  //kept for legacy reasons, do not delete!
  @PostMapping("/popperLaunch")
  public boolean popperLaunch() {
    frontendStatusService.notifyFrontendLaunch();
    return true;
  }

  @PostMapping("/frontendLaunch")
  public boolean frontendLaunch() {
    frontendStatusService.notifyFrontendLaunch();
    return true;
  }

  private Game resolveGame(String table) {
    File tableFile = new File(table.trim());

    // derive the emulator from the table folder
    int emuId = -1;
    for (GameEmulator emu : frontendStatusService.getGameEmulators()) {
      if (StringUtils.startsWithIgnoreCase(tableFile.getAbsolutePath(), emu.getTablesDirectory())) {
        emuId = emu.getId();
        break;
      }
    }

    Game game = gameService.getGameByFilename(emuId, tableFile.getName());
    if (game == null && tableFile.getParentFile() != null) {
      game = gameService.getGameByFilename(emuId, tableFile.getParentFile().getName() + "\\" + tableFile.getName());
    }
    LOG.info("Resource Game Event Handler resolved \"" + game + "\" for table name \"" + table + "\"");
    return game;
  }

}
