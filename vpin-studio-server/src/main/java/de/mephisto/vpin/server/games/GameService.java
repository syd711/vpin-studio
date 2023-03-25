package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.HighscoreType;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.restclient.DeleteDescriptor;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.highscores.*;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.popper.Emulator;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.roms.RomService;
import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.system.DefaultPictureService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
  private PreferencesService preferencesService;

  @Autowired
  private CardService cardService;

  @Autowired
  private AssetService assetService;

  @Autowired
  private DefaultPictureService defaultPictureService;

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

  public List<Game> getGamesByRom(String rom) {
    List<Game> result = new ArrayList<>();
    List<GameDetails> details = gameDetailsRepository.findByRomNameOrTableName(rom, rom);
    for (GameDetails detail : details) {
      result.add(getGame(detail.getPupId()));
    }
    return result;
  }

  public boolean resetGame(int gameId) {
    Game game = this.getGame(gameId);
    if (game == null) {
      return false;
    }
    return highscoreService.resetHighscore(game);
  }

  public boolean deleteGame(@NonNull DeleteDescriptor descriptor) {
    Game game = this.getGame(descriptor.getGameId());
    if (game == null) {
      return false;
    }

    if (descriptor.isDeleteHighscores()) {
      resetGame(game.getId());
    }

    boolean success = true;
    if (descriptor.isDeleteTable()) {
      if (!FileUtils.delete(game.getGameFile())) {
        success = false;
      }

      if (!FileUtils.delete(game.getPOVFile())) {
        success = false;
      }

      if (!FileUtils.delete(game.getResFile())) {
        success = false;
      }
    }

    if (descriptor.isDeleteDirectB2s()) {
      if (!FileUtils.delete(game.getCroppedDefaultPicture())) {
        success = false;
      }
      if (!FileUtils.delete(game.getRawDefaultPicture())) {
        success = false;
      }
      if (!FileUtils.delete(game.getDirectB2SFile())) {
        success = false;
      }
    }

    if (descriptor.isDeletePupPack()) {
      if (game.getPupPack().getPupPackFolder() != null && !FileUtils.deleteFolder(game.getPupPack().getPupPackFolder())) {
        success = false;
      }
    }

    if (descriptor.isDeleteDMDs()) {
      if (!FileUtils.deleteFolder(game.getFlexDMDFolder())) {
        success = false;
      }

      if (!FileUtils.deleteFolder(game.getUltraDMDFolder())) {
        success = false;
      }
    }

    if (descriptor.isDeleteAltSound()) {
      if (game.getAltSoundFolder() != null && !FileUtils.deleteFolder(game.getAltSoundFolder())) {
        success = false;
      }
    }


    if (descriptor.isDeleteAltColor()) {
      if (game.getAltColorFolder() != null && !FileUtils.deleteFolder(game.getAltColorFolder())) {
        success = false;
      }
    }

    if (descriptor.isDeleteCfg()) {
      if (game.getCfgFile() != null && !FileUtils.delete(game.getCfgFile())) {
        success = false;
      }
    }


    if (descriptor.isDeleteMusic()) {
      if (game.getMusicFolder() != null && !FileUtils.deleteFolder(game.getMusicFolder())) {
        success = false;
      }
    }

    GameDetails byPupId = gameDetailsRepository.findByPupId(game.getId());
    if (byPupId != null) {
      gameDetailsRepository.delete(byPupId);
    }

    if (descriptor.isDeleteFromPopper()) {
      if (!pinUPConnector.deleteGame(descriptor.getGameId())) {
        success = false;
      }
    }

    assetService.deleteByExternalId(String.valueOf(descriptor.getGameId()));
    LOG.info("Deleted " + game.getGameDisplayName());
    return success;
  }

  public int getGameCount() {
    return this.pinUPConnector.getGameCount();
  }

  public List<Integer> getGameId() {
    return this.pinUPConnector.getGameIds();
  }

  /**
   * Used for creating the highscore cards combo.
   * We only want to use tables there, that can show a highscore.
   */
  public List<Game> getGamesWithScore() {
    List<Game> games = getGames();
    return games.stream().filter(g -> {
      Optional<Highscore> highscore = highscoreService.getOrCreateHighscore(g);
      return highscore.isPresent() && !StringUtils.isEmpty(highscore.get().getRaw());
    }).collect(Collectors.toList());
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

  /**
   * Retursn the current highscore for the given game
   * @param gameId
   * @return
   */
  public ScoreSummary getScores(int gameId) {
    long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
    return highscoreService.getScoreSummary(serverId, gameId, null);
  }

  /**
   * Returns a complete list of highscore versions
   * @param gameId
   * @return
   */
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

      if (count > 0 && scores.size() == count) {
        return summary;
      }
    }

    return summary;
  }

  /**
   * Returns true if game details are available
   *
   * @param gameId the game to scan
   */
  @Nullable
  public Game scanGame(int gameId) {
    Game game = getGame(gameId);
    if (game != null) {
      Emulator emulator = game.getEmulator();
      if (!emulator.getName().equalsIgnoreCase(Emulator.VISUAL_PINBALL_X)) {
        return game;
      }
      applyGameDetails(game, true);
      highscoreService.scanScore(game);
      defaultPictureService.generateCroppedDefaultPicture(game);

      return getGame(gameId);
    }
    return null;
  }

  @Nullable
  public HighscoreMetadata scanScore(int gameId) {
    Game game = getGame(gameId);
    if (game != null) {
      return highscoreService.scanScore(game);
    }
    return null;
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
      gameDetails.setAssets(StringUtils.join(scanResult.getAssets(), ","));
      gameDetails.setCreatedAt(new java.util.Date());
      gameDetails.setUpdatedAt(new java.util.Date());

      gameDetailsRepository.saveAndFlush(gameDetails);
      LOG.info("Created GameDetails for " + game.getGameDisplayName());

      //check assets too
      defaultPictureService.generateCroppedDefaultPicture(game);
    }

    game.setRom(gameDetails.getRomName());
    game.setNvOffset(gameDetails.getNvOffset());
    game.setAssets(gameDetails.getAssets());
    game.setOriginalRom(romService.getOriginalRom(game.getRom()));
    game.setHsFileName(gameDetails.getHsFileName());
    game.setTableName(gameDetails.getTableName());
    game.setIgnoredValidations(gameDetails.getIgnoredValidations());

    Optional<Highscore> highscore = this.highscoreService.getOrCreateHighscore(game);
    highscore.ifPresent(value -> game.setHighscoreType(value.getType() != null ? HighscoreType.valueOf(value.getType()) : null));

    //run validations at the end!!!
    game.setValidationState(gameValidator.validate(game));
  }

  public Game save(Game game) throws Exception {
    GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
    String existingRom = String.valueOf(gameDetails.getRomName());
    boolean romChanged = !String.valueOf(game.getRom()).equalsIgnoreCase(existingRom);

    gameDetails.setRomName(game.getRom());
    gameDetails.setHsFileName(game.getHsFileName());
    gameDetails.setTableName(game.getTableName());
    gameDetails.setIgnoredValidations(game.getIgnoredValidations());
    gameDetailsRepository.saveAndFlush(gameDetails);

    Game original = getGame(game.getId());
    if (original != null && original.getVolume() != game.getVolume()) {
      pinUPConnector.updateVolume(game, game.getVolume());
    }

    //TODO check rom name import vs. scan
    //check if there is mismatch in the ROM name, overwrite popper value
    if (original != null && !StringUtils.isEmpty(original.getRom()) && !StringUtils.isEmpty(game.getRom()) && !original.getRom().equals(game.getRom())) {
      pinUPConnector.updateRom(game, game.getRom());
    }

    LOG.info("Saved " + game);
    Game updated = getGame(game.getId());
    if (romChanged) {
      highscoreService.scanScore(updated);
      cardService.generateCard(updated, false);
    }
    return updated;
  }
}
