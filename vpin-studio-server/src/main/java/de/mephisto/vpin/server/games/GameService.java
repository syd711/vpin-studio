package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.highscores.*;
import de.mephisto.vpin.server.highscores.cards.CardService;
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

import java.io.File;
import java.util.ArrayList;
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

  @Autowired
  private CardService cardService;

  @SuppressWarnings("unused")
  public List<Game> getGames() {
    long start = System.currentTimeMillis();
    List<Game> games = pinUPConnector.getGames();
    LOG.info("Game fetch took " + (System.currentTimeMillis() - start) + "ms., returned " + games.size() + " tables.");
    start = System.currentTimeMillis();
    for (Game game : games) {
      applyGameDetails(game, false);
    }
    LOG.info("Game details fetch took " + (System.currentTimeMillis() - start) + "ms.");
    return games;
  }

  public boolean deleteGame(int id, boolean vpxDelete, boolean directb2sDelete, boolean popperDelete) {
    Game game = this.getGame(id);
    boolean success = true;
    if (vpxDelete) {
      if(!FileUtils.delete(game.getGameFile())) {
        success = false;
      }
    }

    if (directb2sDelete) {
      if(game.getPOVFile() != null && !FileUtils.delete(game.getPOVFile())) {
        success = false;
      }
      if(!FileUtils.delete(game.getDirectB2SFile())) {
        success = false;
      }
      if(!FileUtils.delete(game.getDirectB2SMediaFile())) {
        success = false;
      }
    }

    if (popperDelete) {
      if(!pinUPConnector.deleteGame(id)) {
        success = false;
      }
    }

    GameDetails byPupId = gameDetailsRepository.findByPupId(game.getId());
    if(byPupId != null) {
      gameDetailsRepository.delete(byPupId);
    }

    highscoreService.deleteScores(game.getId());

    return success;
  }

  public int getGameCount() {
    return this.pinUPConnector.getGameCount();
  }

  public List<Integer> getGameId() {
    return this.pinUPConnector.getGameIds();
  }

  public List<Game> getGamesWithScore() {
    List<Game> games = getGames();
    return games.stream().filter(g -> !StringUtils.isEmpty(highscoreService.getHighscores(g.getId(), g.getGameDisplayName()).getRaw())).collect(Collectors.toList());
  }

  @SuppressWarnings("unused")
  @Nullable
  public Game getGame(int id) {
    Game game = pinUPConnector.getGame(id);
    if (game != null) {
      applyGameDetails(game, false);
      return game;
    }
    return null;
  }

  public ScoreSummary getScores(int gameId) {
    return highscoreService.getHighscores(gameId, null);
  }

  public ScoreList getScoreHistory(int gameId) {
    return highscoreService.getScoreHistory(gameId);
  }

  public ScoreSummary getRecentHighscores(int count) {
    List<Score> scores = new ArrayList<>();
    ScoreSummary summary = new ScoreSummary(scores, null);
    List<Score> allHighscoreVersions = highscoreService.getAllHighscoreVersions();

    //check if the actual game still exists
    for (Score version : allHighscoreVersions) {
      Game game = getGame(version.getGameId());
      if (game != null) {
        scores.add(version);
      }

      if(scores.size() == count) {
        return summary;
      }
    }

    if (scores.size() < count) {
      List<Highscore> highscores = highscoreService.getRecentHighscores();
      for (Highscore highscore : highscores) {
        int gameId = highscore.getGameId();
        Game game = getGame(gameId);
        //check if the actual game still exists
        if (game != null) {
          List<Score> collect = scores.stream().filter(s -> s.getGameId() == gameId).collect(Collectors.toList());

          //only add an current score if no version has been found for this game
          if (collect.isEmpty()) {
            List<Score> versionScores = highscoreService.parseScores(highscore);
            if (!versionScores.isEmpty()) {
              scores.add(versionScores.get(0));
              if (scores.size() == count) {
                break;
              }
            }
          }
        }
      }
    }

    return summary;
  }

  /**
   * Returns true if game details are available
   *
   * @param gameId the game to scan
   * @return
   */
  @Nullable
  public Game scanGame(int gameId) {
    Game game = getGame(gameId);
    if (game != null) {
      Emulator emulator = game.getEmulator();
      if (!emulator.getName().equalsIgnoreCase(Emulator.VISUAL_PINBALL_X)) {
        return game;
      }
      gameDetailsRepository.findByPupId(gameId);
      applyGameDetails(game, true);
      highscoreService.scanScore(game);

      return getGame(gameId);
    }
    return null;
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

  private void applyGameDetails(@NonNull Game game, boolean forceScan) {
    GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
    if (gameDetails == null || forceScan) {
      ScanResult scanResult = romService.scanGameFile(game);

      if (gameDetails == null) {
        gameDetails = new GameDetails();
      }

      gameDetails.setRomName(scanResult.getRom());
      gameDetails.setTableName(scanResult.getTableName());
      gameDetails.setNvOffset(scanResult.getNvOffset());
      gameDetails.setHsFileName(scanResult.getHsFileName());
      gameDetails.setPupId(game.getId());
      gameDetails.setAssets(StringUtils.join(scanResult.getAssets(), "|"));
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
    game.setTableName(gameDetails.getTableName());
    game.setIgnoredValidations(gameDetails.getIgnoredValidations());

//    String assets = gameDetails.getAssets();
//    if(!StringUtils.isEmpty(assets)) {
//      String[] assetNames = assets.split("\\|");
//      for (String assetName : assetNames) {
//        game.getAssets().add(new GameAsset(assetName, false));
//      }
//    }

    //run validations at the end!!!
    game.setValidationState(gameValidator.validate(game));
  }

  public Game save(Game game) throws Exception {
    GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
    boolean romChanged = !String.valueOf(game.getRom()).equalsIgnoreCase(String.valueOf(gameDetails.getRomName()));

    gameDetails.setRomName(game.getRom());
    gameDetails.setHsFileName(game.getHsFileName());
    gameDetails.setTableName(game.getTableName());
    gameDetails.setIgnoredValidations(game.getIgnoredValidations());
    gameDetailsRepository.saveAndFlush(gameDetails);

    Game original = getGame(game.getId());
    if (original.getVolume() != game.getVolume()) {
      pinUPConnector.updateVolume(game, game.getVolume());
    }

    LOG.info("Saved " + game);
    Game updated = getGame(game.getId());
    if(romChanged) {
      highscoreService.updateHighscore(updated);
      cardService.generateCard(updated, false);
    }
    return updated;
  }

}
