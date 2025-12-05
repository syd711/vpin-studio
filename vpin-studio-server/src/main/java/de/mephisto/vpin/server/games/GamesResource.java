package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameScoreValidation;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.highscores.logging.HighscoreEventLog;
import de.mephisto.vpin.restclient.system.FileInfo;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.fp.FuturePinballService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.steam.SteamService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpx.VPXService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "games")
public class GamesResource {
  private final static Logger LOG = LoggerFactory.getLogger(GamesResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private GameMediaService gameMediaService;

  @Autowired
  private VPXService vpxService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private FuturePinballService futurePinballService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameStatusService gameStatusService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private SteamService steamService;

  @GetMapping
  public List<Game> getGames() {
    return gameService.getGames();
  }

  @GetMapping("/ids")
  public List<Integer> getGameIds() {
    return gameService.getGameIds();
  }

  @GetMapping("/reload")
  public boolean reload() {
    return gameService.reload();
  }

  @GetMapping("/reload/{id}")
  public Game reloadGame(@PathVariable("id") int gameId) {
    return gameService.reload(gameId);
  }

  @GetMapping("/reloadEmulator/{emulatorId}")
  public boolean reloadEmulator(@PathVariable("emulatorId") int emulatorId) {
    return gameService.reloadEmulator(emulatorId);
  }

  @GetMapping("/unknowns")
  public List<Integer> getUnknownGameIds() {
    return gameService.getUnknownGames();
  }

  @GetMapping("/knowns/{emulatorId}")
  public List<Game> getKnownGames(@PathVariable("emulatorId") int emulatorId) {
    return gameService.getKnownGames(emulatorId);
  }

  @PutMapping("/play/{id}")
  public boolean play(@PathVariable("id") int id, @RequestBody Map<String, Object> values) {
    Game game = gameService.getGame(id);
    try {
      systemService.setMaintenanceMode(false);
      String altExe = (String) values.get("altExe");
      String option = (String) values.get("option");

      GameEmulator gameEmulator = emulatorService.getGameEmulator(game.getEmulatorId());
      if (gameEmulator == null) {
        return false;
      }

      EmulatorType type = gameEmulator.getType();
      if (game.isVpxGame()) {
        frontendService.killFrontend();
        if (vpxService.play(game, altExe, option)) {
          gameStatusService.setActiveStatus(id);
          return true;
        }
      }
      else if (game.isFpGame()) {
        frontendService.killFrontend();
        if (futurePinballService.play(game, altExe)) {
          gameStatusService.setActiveStatus(id);
          return true;
        }
      }
      else if (game.isZenGame() || game.isZaccariaGame()) {
        frontendService.killFrontend();
        if (steamService.play(game)) {
          gameStatusService.setActiveStatus(id);
          return true;
        }
      }
      throw new UnsupportedOperationException("Unsupported emulator: " + game.getEmulator());
    }
    finally {
      gameLifecycleService.notifyGameUpdated(game.getId());
    }
  }

  @GetMapping("/recent/{count}")
  public ScoreSummary getRecentHighscores(@PathVariable("count") int count) {
    return gameService.getRecentHighscores(count);
  }

  @GetMapping("/recent/{count}/{gameId}")
  public ScoreSummary getRecentHighscoresForGame(@PathVariable("count") int count, @PathVariable("gameId") int gameId) {
    return gameService.getRecentHighscores(count, gameId);
  }

  @GetMapping("/{id}")
  public Game getGame(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      throw new ResponseStatusException(NOT_FOUND, "Not game found for id " + id);
    }
    return game;
  }

  @GetMapping("/validations/{id}")
  public List<ValidationState> getAllValidations(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    if (game == null) {
      return Collections.emptyList();
    }
    return gameService.validate(game);
  }

  @GetMapping("/eventlog/{id}")
  public HighscoreEventLog getEventLog(@PathVariable("id") int id) {
    return gameService.getEventLog(id);
  }

  @GetMapping("/scorevalidation/{id}")
  public GameScoreValidation getGameScoreValidation(@PathVariable("id") int id) {
    return gameService.getGameScoreValidation(id);
  }

  @PostMapping("/scorevalidation/{id}")
  public GameScoreValidation getGameScoreValidation(@PathVariable("id") int id, @RequestBody TableDetails tableDetails) {
    return gameService.getGameScoreValidation(id, tableDetails);
  }

  @GetMapping("/scores/{id}")
  public ScoreSummary getScores(@PathVariable("id") int gameId) {
    return gameService.getScores(gameId);
  }

  @GetMapping("/highscorefiles/{id}")
  public HighscoreFiles getHighscoreFiles(@PathVariable("id") int id) {
    return gameService.getHighscoreFiles(id);
  }

  @GetMapping("/highscorefile/{id}/fileinfo")
  public FileInfo getHighscoreFileInfo(@PathVariable("id") int gameId) {
    File hsfile = gameService.getHighscoreFile(gameId);
    return hsfile != null ? FileInfo.file(hsfile, hsfile.getParentFile()) : null;
  }

  @GetMapping("/scorehistory/{id}")
  public ScoreList getScoreHistory(@PathVariable("id") int id) {
    return gameService.getScoreHistory(id);
  }

  /**
   * This scan is only triggered through the initial installation.
   * We skip the highscore scan for the initial GameDetails creation and execute the score parsing afterwards.
   */
  @GetMapping("/scan/{id}")
  public Game scanGame(@PathVariable("id") int pupId) {
    LOG.info("Client initiated game scan for " + pupId);
    Game game = gameService.scanGame(pupId);
    if (game != null) {
      gameService.scanScore(game.getId(), EventOrigin.INITIAL_SCAN);
    }
    return game;
  }

  @GetMapping("/scanscore/{id}")
  public HighscoreMetadata scanGameScore(@PathVariable("id") int pupId) {
    return gameService.scanScore(pupId, EventOrigin.USER_INITIATED);
  }

  @PostMapping("/delete")
  public boolean delete(@RequestBody DeleteDescriptor descriptor) {
    return gameMediaService.deleteGame(descriptor);
  }

  @PostMapping("/deleteGameFile")
  public boolean deleteGameFile(@RequestBody Map<String, Object> data) {
    int emulatorId = (int) data.get("emulatorId");
    String fileName = (String) data.get("fileName");
    return gameMediaService.deleteGameFile(emulatorId, fileName);
  }

  @PostMapping("/reset")
  public boolean reset(@RequestBody Map<String, Long> values) {
    long gameId = values.get("gameId");
    long score = values.get("scoreValue");
    return gameService.resetGame((int) gameId, score);
  }

  @PostMapping("/save")
  public Game save(@RequestBody Game game) throws Exception {
    return gameService.save(game);
  }

  @PostMapping("/match")
  public Game findMatch(@RequestBody Map<String, String> params) throws Exception {
    String term = params.get("term");
    return gameService.findMatch(term);
  }
}
