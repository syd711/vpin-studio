package de.mephisto.vpin.server.games;

import de.mephisto.vpin.server.fx.OverlayWindowFX;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.roms.RomService;
import de.mephisto.vpin.server.roms.ScanResult;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameService implements InitializingBean {
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

  @Override
  public void afterPropertiesSet() {
    new Thread(() -> {
      OverlayWindowFX.main(new String[]{});
      LOG.info("Overlay listener started.");
    }).start();
  }


  @SuppressWarnings("unused")
  public List<Game> getGames() {
    long start = System.currentTimeMillis();
    List<Game> games = pinUPConnector.getGames();
    LOG.info("Game fetch took " + (System.currentTimeMillis() - start) + "ms.");
    start = System.currentTimeMillis();
    for (Game game : games) {
      loadGameDetails(game, games);
    }
    LOG.info("Game details fetch took " + (System.currentTimeMillis() - start) + "ms.");
    return games;
  }

  @SuppressWarnings("unused")
  public Game getGame(int id) {
    Game game = pinUPConnector.getGame(id);
    List<Game> games = pinUPConnector.getGames();
    loadGameDetails(game, games);
    return game;
  }

  public boolean scanGame(int gameId) {
    Game game = getGame(gameId);
    List<Game> games = pinUPConnector.getGames();
    ScanResult scanResult = romService.scanGameFile(game);

    gameDetailsRepository.findByPupId(gameId);
    GameDetails gameDetails = loadGameDetails(game, games);
    if (gameDetails != null) {
      gameDetails.setRomName(scanResult.getRom());
      gameDetails.setNvOffset(scanResult.getNvOffset());
      gameDetails.setHsFileName(scanResult.getHsFileName());
      gameDetailsRepository.saveAndFlush(gameDetails);
      return true;
    }
    return false;
  }

  @SuppressWarnings("unused")
  public List<Game> getActiveGameInfos() {
    List<Integer> gameIdsFromPlaylists = this.pinUPConnector.getGameIdsFromPlaylists();
    List<Game> games = pinUPConnector.getGames();
    return games.stream().filter(g -> gameIdsFromPlaylists.contains(g.getId())).collect(Collectors.toList());
  }

  public List<Game> getGameInfos() {
    return pinUPConnector.getGames();
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

  @Nullable
  public Game getGameByRom(@NonNull String romName) {
    List<Game> games = pinUPConnector.getGames();
    for (Game game : games) {
      if (game.getRom() != null && game.getRom().equals(romName)) {
        return game;
      }
    }
    return null;
  }

  @SuppressWarnings("unused")
  public Game getGameByName(String table) {
    return this.pinUPConnector.getGameByName(table);
  }

  public Game getGameByFile(File file) {
    return this.pinUPConnector.getGameByFilename(file.getName());
  }

  @Nullable
  private GameDetails loadGameDetails(@NonNull Game game, @NonNull List<Game> games) {
    try {
      GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
      if (gameDetails == null) {
        gameDetails = new GameDetails();
        gameDetails.setPupId(game.getId());
        gameDetails.setCreatedAt(new java.util.Date());
        gameDetails.setUpdatedAt(new java.util.Date());
        gameDetailsRepository.saveAndFlush(gameDetails);
      }

      game.setNvOffset(gameDetails.getNvOffset());
      if (StringUtils.isEmpty(game.getRom())) {
        game.setRom(gameDetails.getRomName());
      }
      game.setOriginalRom(romService.getOriginalRom(game.getRom()));
      game.setHsFileName(gameDetails.getHsFileName());
      game.setIgnoredValidations(gameDetails.getIgnoredValidations());

      Highscore highscore = highscoreService.getHighscore(game);
      if (highscore != null) {
        game.setRawHighscore(highscore.getRaw());
      }

      //run validations at the end!!!
      game.setValidationState(gameValidator.validate(game, games));
      return gameDetails;
    } catch (Exception e) {
      LOG.error("Failed to load details for " + game + ": " + e.getMessage(), e);
    }
    return null;
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
