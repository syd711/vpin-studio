package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameDetailsRepresentation;
import de.mephisto.vpin.restclient.games.GameScoreValidation;
import de.mephisto.vpin.restclient.games.GameValidationStateFactory;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetRepository;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.highscores.*;
import de.mephisto.vpin.server.mame.MameRomAliasService;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.popper.GameMediaItem;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.popper.PopperService;
import de.mephisto.vpin.server.popper.WheelAugmenter;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.roms.RomService;
import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vps.VpsService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
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
  private GameValidationService gameValidator;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private HighscoreBackupService highscoreBackupService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private AssetRepository assetRepository;

  @Autowired
  private PupPacksService pupPackService;

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private AltColorService altColorService;

  @Autowired
  private MameRomAliasService mameRomAliasService;

  @Autowired
  private VpsService vpsService;

  @Autowired
  private ScoreFilter scoreFilter;

  @Autowired
  private PlayerService playerService;

  @Autowired
  private SystemService systemService;

  @Autowired
  private MameService mameService;

  @Autowired
  private PopperService popperService;

  @Deprecated //do not use because of lazy scanning
  public List<Game> getGames() {
    long start = System.currentTimeMillis();
    List<Game> games = new ArrayList<>(pinUPConnector.getGames());
    LOG.info("Game fetch took " + (System.currentTimeMillis() - start) + "ms., returned " + games.size() + " tables.");
    start = System.currentTimeMillis();

    for (Game game : games) {
      applyGameDetails(game, null, false);
    }
    LOG.info("Game details fetch took " + (System.currentTimeMillis() - start) + "ms.");
    return games;
  }

  public Game getGameByVpsTable(@NonNull String vpsTableId, @Nullable String vpsTableVersionId) {
    List<Game> knownGames = getKnownGames();
    Game hit = null;
    for (Game game : knownGames) {
      if (!StringUtils.isEmpty(game.getExtTableId()) && game.getExtTableId().equals(vpsTableId)) {
        if (vpsTableVersionId == null) {
          hit = game;
          break;
        }

        if (!StringUtils.isEmpty(game.getExtTableVersionId()) && game.getExtTableVersionId().equals(vpsTableVersionId)) {
          hit = game;
          break;
        }
      }
    }
    return hit;
  }

  /**
   * Pre-reload triggered before an actual manual table reload (server service cache reset)
   */
  public boolean reload() {
    pinUPConnector.loadEmulators();
    mameRomAliasService.clearCache();
    highscoreService.refreshAvailableScores();
    return true;
  }

  @SuppressWarnings("unused")
  public List<Integer> getUnknownGames() {
    List<Integer> gameIds = new ArrayList<>(getGameIds());
    List<Integer> filtered = new ArrayList<>();
    for (Integer id : gameIds) {
      GameDetails gameDetails = gameDetailsRepository.findByPupId(id);
      if (gameDetails == null) {
        filtered.add(id);
      }
    }
    return filtered;
  }

  @SuppressWarnings("unused")
  public List<Game> getKnownGames() {
    List<Game> games = new ArrayList<>(pinUPConnector.getGames());
    boolean killedPopper = false;
    for (Game game : games) {
      GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
      if (gameDetails != null) {
        applyGameDetails(game, gameDetails, false);
      }
      else {
        if (!killedPopper) {
          LOG.info("New games have been found, automatically killing popper to release locks.");
          systemService.killPopper();
          killedPopper = true;
        }
      }
    }
    return games;
  }

  public List<Game> getGamesByRom(@NonNull String rom) {
    List<Game> games = this.getGames()
        .stream()
        .filter(g ->
            (!StringUtils.isEmpty(g.getRom()) && g.getRom().equalsIgnoreCase(rom)) ||
                (!StringUtils.isEmpty(g.getTableName()) && g.getTableName().equalsIgnoreCase(rom)))
        .collect(Collectors.toList());
    for (Game game : games) {
      applyGameDetails(game, null, false);
    }
    return games;
  }

  public boolean resetGame(int gameId) {
    Game game = this.getGame(gameId);
    if (game == null) {
      return false;
    }


    if (highscoreBackupService.backup(game)) {
      return highscoreService.resetHighscore(game);
    }

    return false;
  }

  public boolean deleteGame(@NonNull DeleteDescriptor descriptor) {
    List<Integer> gameIds = descriptor.getGameIds();
    boolean success = true;

    for (Integer gameId : gameIds) {
      Game game = this.getGame(gameId);
      if (game == null) {
        return false;
      }

      if (descriptor.isDeleteHighscores()) {
        highscoreService.deleteScores(game.getId(), true);
      }

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

        if (!FileUtils.delete(game.getIniFile())) {
          success = false;
        }

        if (!FileUtils.delete(game.getVBSFile())) {
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
        PupPack pupPack = game.getPupPack();
        if (pupPack != null && !pupPack.delete()) {
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
        if (altSoundService.delete(game)) {
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

        List<Game> gamesByRom = new ArrayList<>(getGamesByRom(game.getRom()));
        if (gamesByRom.size() == 1) {
          if (!mameService.deleteOptions(game.getRom())) {
            success = false;
          }
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
        if (!pinUPConnector.deleteGame(gameId)) {
          success = false;
        }

        //only delete the popper assets, if there is no other game with the same "Game Name".
        List<Game> allOtherTables = this.pinUPConnector.getGames().stream().filter(g -> g.getId() != game.getId()).collect(Collectors.toList());
        List<Game> duplicateGameNameTables = allOtherTables.stream().filter(t -> t.getGameName().equalsIgnoreCase(game.getGameName())).collect(Collectors.toList());

        if (duplicateGameNameTables.isEmpty()) {
          PopperScreen[] values = PopperScreen.values();
          for (PopperScreen originalScreenValue : values) {
            List<GameMediaItem> gameMediaItem = game.getGameMedia().getMediaItems(originalScreenValue);
            for (GameMediaItem mediaItem : gameMediaItem) {
              File mediaFile = mediaItem.getFile();

              if (originalScreenValue.equals(PopperScreen.Wheel)) {
                WheelAugmenter augmenter = new WheelAugmenter(mediaFile);
                augmenter.deAugment();
              }

              if (!mediaFile.delete()) {
                success = false;
              }
              else {
                LOG.warn("Failed to delete \"" + mediaFile.getAbsolutePath() + "\" for \"" + game.getGameDisplayName() + "\"");
              }
            }
          }
        }
        else {
          LOG.info("Deletion of Popper assets has been skipped, because there are " + duplicateGameNameTables.size() + " tables with the same GameName \"" + game.getGameName() + "\"");
        }
      }

      Optional<Asset> byId = assetRepository.findByExternalId(String.valueOf(gameId));
      byId.ifPresent(asset -> assetRepository.delete(asset));

      File gameFolder = game.getGameFile().getParentFile();
      if (gameFolder.exists()) {
        String[] list = gameFolder.list();
        if (list == null || list.length == 0) {
          if (gameFolder.delete()) {
            LOG.info("Deleted table folder " + gameFolder.getAbsolutePath());
          }
        }
      }

      LOG.info("Deleted \"" + game.getGameDisplayName() + "\"");
    }
    return success;
  }

  public List<Integer> getGameIds() {
    return this.pinUPConnector.getGameIds();
  }

  @SuppressWarnings("unused")
  @Nullable
  public synchronized Game getGame(int id) {
    Game game = pinUPConnector.getGame(id);
    if (game != null) {
      applyGameDetails(game, null, false);
      return game;
    }
    return null;
  }

  /**
   * Returns the current highscore for the given game
   */
  public ScoreSummary getScores(int gameId) {
    long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
    return highscoreService.getScoreSummary(serverId, getGame(gameId));
  }

  /**
   * Returns a complete list of highscore versions
   */
  public ScoreList getScoreHistory(int gameId) {
    return highscoreService.getScoreHistory(gameId);
  }

  public ScoreSummary getRecentHighscores(int count) {
    return getRecentHighscores(count, -1);
  }

  public ScoreSummary getRecentHighscores(int count, int gameId) {
    long start = System.currentTimeMillis();
    List<Score> scores = new ArrayList<>();
    ScoreSummary summary = new ScoreSummary(scores, null);

    boolean filterEnabled = (boolean) preferencesService.getPreferenceValue(PreferenceNames.HIGHSCORE_FILTER_ENABLED, false);
    List<Player> buildInPlayers = playerService.getBuildInPlayers();
    List<Score> allHighscoreVersions = new ArrayList<>();

    if (!filterEnabled || buildInPlayers.isEmpty()) {
      allHighscoreVersions.addAll(highscoreService.getAllHighscoreVersions(null));
    }
    else {

      for (Player buildInPlayer : buildInPlayers) {
        allHighscoreVersions.addAll(highscoreService.getAllHighscoreVersions(buildInPlayer.getInitials()));
      }
    }

    //check if the actual game still exists
    for (Score version : allHighscoreVersions) {
      //filter by game
      if (gameId > 0 && version.getGameId() != gameId) {
        continue;
      }

      if (scoreFilter.isScoreFiltered(version)) {
        continue;
      }

      Game rawGame = pinUPConnector.getGame(version.getGameId());
      if (rawGame != null && !scores.contains(version)) {
        scores.add(version);
      }

      if (count > 0 && scores.size() == count) {
        return summary;
      }
    }

    long duration = System.currentTimeMillis() - start;
    LOG.info("Recent score fetch took " + duration + "ms.");
    return summary;
  }

  @Nullable
  public Game scanGame(int gameId) {
    Game game = null;
    try {
      game = pinUPConnector.getGame(gameId);
      if (game != null) {
        applyGameDetails(game, null, true);
        if (game.isVpxGame()) {
          highscoreService.scanScore(game);
        }
        return getGame(gameId);
      }
      else {
        LOG.error("No game found to be scanned with ID '" + gameId + "'");
      }
    }
    catch (Exception e) {
      if (game != null) {
        LOG.error("Game scan for \"" + game.getGameDisplayName() + "\" (" + gameId + ") failed: " + e.getMessage(), e);
      }
      else {
        LOG.error("Game scan for game " + gameId + " failed: " + e.getMessage(), e);
      }
    }
    return game;
  }

  @Nullable
  public HighscoreMetadata scanScore(int gameId) {
    Game game = getGame(gameId);
    if (game != null) {
      return highscoreService.scanScore(game);
    }
    return null;
  }

  @Nullable
  public GameDetailsRepresentation getGameDetails(int gameId) {
    GameDetailsRepresentation details = new GameDetailsRepresentation();
    GameDetails ref = gameDetailsRepository.findByPupId(gameId);
    details.setRomName(ref.getRomName());
    details.setHsFileName(ref.getHsFileName());
    details.setTableName(ref.getTableName());
    return details;
  }

  @SuppressWarnings("unused")
  public List<Game> getActiveGameInfos() {
    List<Integer> gameIdsFromPlaylists = this.pinUPConnector.getGameIdsFromPlaylists();
    List<Game> games = pinUPConnector.getGames();
    return games.stream().filter(g -> gameIdsFromPlaylists.contains(g.getId())).collect(Collectors.toList());
  }

  public Game getGameByFilename(String name) {
    Game game = this.pinUPConnector.getGameByFilename(name);
    if (game != null) {
      //this will ensure that a scanned table is fetched
      game = this.getGame(game.getId());
    }
    return game;
  }

  public Game getGameByName(String name) {
    Game game = this.pinUPConnector.getGameByName(name);
    if (game != null) {
      //this will ensure that a scanned table is fetched
      game = this.getGame(game.getId());
    }
    return game;
  }

  private synchronized void applyGameDetails(@NonNull Game game, @Nullable GameDetails gameDetails, boolean forceScan) {
    if (gameDetails == null) {
      gameDetails = gameDetailsRepository.findByPupId(game.getId());
    }

    if (!game.isVpxGame()) {
      if (gameDetails == null) {
        gameDetails = new GameDetails();
        gameDetails.setCreatedAt(new Date());
        gameDetails.setUpdatedAt(new Date());
        gameDetails.setPupId(game.getId());
        gameDetails = gameDetailsRepository.saveAndFlush(gameDetails);
      }

      game.setIgnoredValidations(ValidationState.toIds(gameDetails.getIgnoredValidations()));
      List<ValidationState> validate = gameValidator.validate(game, true);
      if (validate.isEmpty()) {
        validate.add(GameValidationStateFactory.empty());
      }
      game.setValidationState(validate.get(0));
      game.setNotes(gameDetails.getNotes());
      return;
    }

    if (gameDetails == null || forceScan) {
      ScanResult scanResult = romService.scanGameFile(game);

      if (gameDetails == null) {
        gameDetails = new GameDetails();
      }

      //always prefer PinUP Popper ROM name over the scanned value
      String scannedRomName = scanResult.getRom();
      String scannedTableName = scanResult.getTableName();

      TableDetails tableDetails = pinUPConnector.getTableDetails(game.getId());
      if (StringUtils.isEmpty(scannedRomName) && !StringUtils.isEmpty(tableDetails.getRomName())) {
        scannedRomName = tableDetails.getRomName();
      }

      if (StringUtils.isEmpty(scannedTableName) && !StringUtils.isEmpty(tableDetails.getRomAlt())) {
        scannedTableName = tableDetails.getRomAlt();
      }

      gameDetails.setRomName(scannedRomName);
      gameDetails.setTableName(scannedTableName);
      gameDetails.setNvOffset(scanResult.getNvOffset());
      gameDetails.setHsFileName(scanResult.getHsFileName());
      gameDetails.setPupId(game.getId());
      gameDetails.setPupPack(scanResult.getPupPackName());
      gameDetails.setAssets(StringUtils.join(scanResult.getAssets(), ","));

      gameDetails.setCreatedAt(new java.util.Date());
      gameDetails.setUpdatedAt(new java.util.Date());

      gameDetailsRepository.saveAndFlush(gameDetails);
      LOG.info("Created GameDetails for " + game.getGameDisplayName() + ", was forced: " + forceScan);
    }


    //only apply legacy table name if the Popper fields are empty
    if (StringUtils.isEmpty(game.getTableName())) {
      game.setTableName(gameDetails.getTableName());
    }

    if (StringUtils.isEmpty(game.getRom())) {
      game.setRom(gameDetails.getRomName());
    }

    //check alias
    String originalRom = mameRomAliasService.getRomForAlias(game.getEmulator(), game.getRom());
    if (!StringUtils.isEmpty(originalRom)) {
      String aliasName = game.getRom();
      game.setRom(originalRom);
      game.setRomAlias(aliasName);
    }

    game.setNvOffset(gameDetails.getNvOffset());

    //only apply legacy highscore name if the Popper fields are empty
    if (StringUtils.isEmpty(game.getHsFileName())) {
      game.setHsFileName(gameDetails.getHsFileName());
    }

    //only apply legacy VPS data if the Popper fields are empty
    if (StringUtils.isEmpty(game.getExtTableId())) {
      game.setExtTableId(gameDetails.getExtTableId());
    }
    if (StringUtils.isEmpty(game.getExtTableVersionId())) {
      game.setExtTableVersionId(gameDetails.getExtTableVersionId());
    }

    game.setTemplateId(gameDetails.getTemplateId());
    game.setNotes(gameDetails.getNotes());

    //PUP pack assignment: we have to differ between the scanned name and the actual resolved one which could be different.
    game.setPupPackName(gameDetails.getPupPack());
    PupPack pupPack = pupPackService.getPupPack(game);
    if (pupPack != null) {
      game.setPupPack(pupPack);
      game.setPupPackName(pupPack.getName());
    }

    game.setIgnoredValidations(ValidationState.toIds(gameDetails.getIgnoredValidations()));
    game.setAltSoundAvailable(altSoundService.isAltSoundAvailable(game));
    game.setAltColorType(altColorService.getAltColorType(game));


    String updates = gameDetails.getUpdates();
    game.setVpsUpdates(VPSChanges.fromJson(updates));
    vpsService.applyVersionInfo(game);

    Optional<Highscore> highscore = this.highscoreService.getOrCreateHighscore(game);
    highscore.ifPresent(value -> game.setHighscoreType(value.getType() != null ? HighscoreType.valueOf(value.getType()) : null));

    //run validations at the end!!!
    List<ValidationState> validate = gameValidator.validate(game, true);
    if (validate.isEmpty()) {
      validate.add(GameValidationStateFactory.empty());
    }
    game.setValidationState(validate.get(0));
  }

  public List<ValidationState> validate(Game game) {
    return gameValidator.validate(game, false);
  }

  public synchronized Game save(Game game) throws Exception {
    GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
    gameDetails.setTemplateId(game.getTemplateId());
    gameDetails.setNotes(game.getNotes());
    gameDetails.setIgnoredValidations(ValidationState.toIdString(game.getIgnoredValidations()));
    if (game.getVpsUpdates() != null) {
      VPSChanges vpsUpdates = game.getVpsUpdates();
      String json = vpsUpdates.toJson();
      gameDetails.setUpdates(json);
    }
    gameDetailsRepository.saveAndFlush(gameDetails);
    LOG.info("Saved \"" + game.getGameDisplayName() + "\"");
    return getGame(game.getId());
  }

  public void resetUpdate(int gameId, VpsDiffTypes diffType) {
    try {
      GameDetails gameDetails = gameDetailsRepository.findByPupId(gameId);
      String updates = gameDetails.getUpdates();
      if (updates != null) {
        List<String> existingUpdates = new ArrayList<>(Arrays.asList(updates.split(",")));
        existingUpdates.remove(diffType.name());
        updates = String.join(",", existingUpdates);
        gameDetails.setUpdates(updates);
        gameDetailsRepository.saveAndFlush(gameDetails);
        LOG.info("Resetted updates for " + gameId + " and removed \"" + diffType + "\", new update list: \"" + updates.trim() + "\"");
      }
    }
    catch (Exception e) {
      LOG.error("Failed to reset update flag for " + gameId + ": " + e.getMessage(), e);
    }
  }

  public HighscoreFiles getHighscoreFiles(int id) {
    Game game = getGame(id);
    if (game.isVpxGame()) {
      return highscoreService.getHighscoreFiles(game);
    }
    return new HighscoreFiles();
  }

  public GameScoreValidation getGameScoreValidation(int id) {
    Game game = getGame(id);
    GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
    TableDetails tableDetails = pinUPConnector.getTableDetails(id);
    return gameValidator.validateHighscoreStatus(game, gameDetails, tableDetails);
  }

  public void installRom(UploadDescriptor uploadDescriptor, File tempFile) throws IOException {
    GameEmulator gameEmulator = popperService.getGameEmulator(uploadDescriptor.getEmulatorId());
    File out = new File(gameEmulator.getRomFolder(), uploadDescriptor.getRom());
    org.apache.commons.io.FileUtils.copyFile(tempFile, out);
  }

  @Override
  public void afterPropertiesSet() throws Exception {

  }
}
