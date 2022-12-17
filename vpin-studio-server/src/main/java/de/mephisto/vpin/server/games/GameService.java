package de.mephisto.vpin.server.games;

import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.popper.Emulator;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.roms.RomService;
import de.mephisto.vpin.server.roms.ScanResult;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService {
  private final static Logger LOG = LoggerFactory.getLogger(GameService.class);

  @Autowired
  private PinUPConnector pinUPConnector;

  @Autowired
  private RomService romService;

  @Autowired
  private GameDetailsRepository gameDetailsRepository;

  @Autowired
  private GameValidator gameValidator;

  @Autowired
  private HighscoreService highscoreService;


  @SuppressWarnings("unused")
  public List<Game> getGames() {
    long start = System.currentTimeMillis();
    List<Game> games = pinUPConnector.getGames();
    LOG.info("Game fetch took " + (System.currentTimeMillis() - start) + "ms., returned " + games.size() + " tables.");
    start = System.currentTimeMillis();
    for (Game game : games) {
      applyGameDetails(game, games, false);
    }
    LOG.info("Game details fetch took " + (System.currentTimeMillis() - start) + "ms.");
    return games;
  }

  public int getGameCount() {
    return this.pinUPConnector.getGameCount();
  }

  public List<Game> getGamesWithScore() {
    List<Game> games = getGames();
    return games.stream().filter(g -> !StringUtils.isEmpty(highscoreService.getHighscores(g.getId()).getRaw())).collect(Collectors.toList());
  }

  @SuppressWarnings("unused")
  public Game getGame(int id) {
    Game game = pinUPConnector.getGame(id);
    if (game != null) {
      List<Game> games = pinUPConnector.getGames();
      applyGameDetails(game, games, false);
      return game;
    }
    return null;
  }

  public ScoreSummary getScores(int gameId) {
    return highscoreService.getHighscores(gameId);
  }

  public ScoreSummary getRecentHighscores(int count) {
    return highscoreService.getRecentHighscores();
  }

  /**
   * Returns true if game details are available
   *
   * @param gameId the game to scan
   * @return
   */
  public boolean scanGame(int gameId) {
    Game game = getGame(gameId);
    if (!game.getEmulator().getName().equalsIgnoreCase(Emulator.VISUAL_PINBALL_X)) {
      return false;
    }
    List<Game> games = pinUPConnector.getGames();

    gameDetailsRepository.findByPupId(gameId);
    applyGameDetails(game, games, true);
    highscoreService.scanScore(game);

    return true;
  }

  public HighscoreMetadata scanScore(int gameId) {
    Game game = getGame(gameId);
    return highscoreService.scanScore(game);
  }

  @SuppressWarnings("unused")
  public List<Game> getActiveGameInfos() {
    List<Integer> gameIdsFromPlaylists = this.pinUPConnector.getGameIdsFromPlaylists();
    List<Game> games = pinUPConnector.getGames();
    return games.stream().filter(g -> gameIdsFromPlaylists.contains(g.getId())).collect(Collectors.toList());
  }

  @SuppressWarnings("unused")
  @Nullable
  public Game getGameByVpxFilename(@NonNull String filename) {
    List<Game> games = pinUPConnector.getGames();
    for (Game game : games) {
      if (game.getGameFile().getName().equals(filename)) {
        return game;
      }
    }
    return null;
  }

  public Game getGameByFilename(String name) {
    Game game = this.pinUPConnector.getGameByFilename(name);
    if (game != null) {
      //this will ensure that a scanned table is fetched
      game = this.getGame(game.getId());
    }
    return game;
  }

  private void applyGameDetails(@NonNull Game game, @NonNull List<Game> games, boolean forceScan) {
    GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
    if (gameDetails == null || forceScan) {
      ScanResult scanResult = romService.scanGameFile(game);

      if(gameDetails == null) {
        gameDetails = new GameDetails();
      }

      gameDetails.setRomName(scanResult.getRom());
      gameDetails.setNvOffset(scanResult.getNvOffset());
      gameDetails.setHsFileName(scanResult.getHsFileName());
      gameDetails.setPupId(game.getId());
      gameDetails.setCreatedAt(new java.util.Date());
      gameDetails.setUpdatedAt(new java.util.Date());

      gameDetailsRepository.saveAndFlush(gameDetails);
      LOG.info("Created GameDetails for " + game.getGameDisplayName());
    }

    game.setNvOffset(gameDetails.getNvOffset());
    if (StringUtils.isEmpty(game.getRom()) && !StringUtils.isEmpty(gameDetails.getRomName())) {
      game.setRom(gameDetails.getRomName());

      //re-fetch highscore since the ROM may be set
      this.highscoreService.getOrCreateHighscore(game);
    }
    game.setOriginalRom(romService.getOriginalRom(game.getRom()));
    game.setHsFileName(gameDetails.getHsFileName());
    game.setIgnoredValidations(gameDetails.getIgnoredValidations());

    //run validations at the end!!!
    game.setValidationState(gameValidator.validate(game, games));
  }

  public Game save(Game game) {
    GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
    gameDetails.setRomName(game.getRom());
    gameDetails.setHsFileName(game.getHsFileName());
    gameDetails.setIgnoredValidations(game.getIgnoredValidations());
    gameDetailsRepository.saveAndFlush(gameDetails);

    Game original = getGame(game.getId());
    if (original.getVolume() != game.getVolume()) {
      pinUPConnector.updateVolume(game, game.getVolume());
    }

    LOG.info("Saved " + game);
    return getGame(game.getId());
  }
}
