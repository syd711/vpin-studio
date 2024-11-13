package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.GameScoreValidation;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.highscores.logging.HighscoreEventLog;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.fp.FPService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpx.VPXService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
  private FPService fpService;

  @Autowired
  private SystemService systemService;

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
    systemService.setMaintenanceMode(false);

    String altExe = (String) values.get("altExe");
    Game game = gameService.getGame(id);
    if (game.getEmulator().isVpxEmulator()) {
      frontendService.killFrontend();
      return vpxService.play(game, altExe);
    }
    else if (game.getEmulator().isFpEmulator()) {
      frontendService.killFrontend();
      return fpService.play(game, altExe);
    }
    throw new UnsupportedOperationException("Unsupported emulator: " + game.getEmulator());
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

  @GetMapping("/scores/{id}")
  public ScoreSummary getScores(@PathVariable("id") int id) {
    return gameService.getScores(id);
  }

  @GetMapping("/highscorefiles/{id}")
  public HighscoreFiles getHighscoreFiles(@PathVariable("id") int id) {
    return gameService.getHighscoreFiles(id);
  }

  @GetMapping("/scorehistory/{id}")
  public ScoreList getScoreHistory(@PathVariable("id") int id) {
    return gameService.getScoreHistory(id);
  }

  @GetMapping("/scan/{id}")
  public Game scanGame(@PathVariable("id") int pupId) {
    LOG.info("Client initiated game scan for " + pupId);
    return gameService.scanGame(pupId);
  }

  @GetMapping("/scanscore/{id}")
  public HighscoreMetadata scanGameScore(@PathVariable("id") int pupId) {
    return gameService.scanScore(pupId, EventOrigin.USER_INITIATED);
  }

  @PostMapping("/delete")
  public boolean delete(@RequestBody DeleteDescriptor descriptor) {
    return gameMediaService.deleteGame(descriptor);
  }

  @DeleteMapping("/reset/{gameId}")
  public boolean reset(@PathVariable("gameId") int gameId) {
    return gameService.resetGame(gameId);
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
