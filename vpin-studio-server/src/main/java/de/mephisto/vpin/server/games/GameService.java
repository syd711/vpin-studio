package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.StringSimilarity;
import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.*;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.highscores.logging.HighscoreEventLog;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.highscores.*;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.mame.MameRomAliasService;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.roms.RomService;
import de.mephisto.vpin.server.roms.ScanResult;
import de.mephisto.vpin.server.system.DefaultPictureService;
import de.mephisto.vpin.server.vps.VpsService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(GameService.class);

  private static final double MATCHING_THRESHOLD = 0.1;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private RomService romService;

  @Autowired
  private GameDetailsRepository gameDetailsRepository;

  @Autowired
  private GameValidationService gameValidationService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private HighscoreBackupService highscoreBackupService;

  @Autowired
  private PreferencesService preferencesService;

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
  private MameService mameService;

  @Autowired
  private DefaultPictureService defaultPictureService;

  @Deprecated //do not use because of lazy scanning
  public List<Game> getGames() {
    long start = System.currentTimeMillis();
    List<Game> games = new ArrayList<>(frontendService.getGames());
    LOG.info("Game fetch took " + (System.currentTimeMillis() - start) + "ms., returned " + games.size() + " tables.");
    start = System.currentTimeMillis();

    for (Game game : games) {
      applyGameDetails(game, false, false);
    }
    LOG.info("Game details fetch took " + (System.currentTimeMillis() - start) + "ms.");
    return games;
  }

  public Game getGameByVpsTable(@NonNull String vpsTableId, @Nullable String vpsTableVersionId) {
    List<Game> knownGames = getKnownGames(-1);
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
    frontendService.loadEmulators();
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
  public List<Game> getKnownGames(int emulatorId) {
    long start = System.currentTimeMillis();
    List<Game> games = new ArrayList<>();
    if (emulatorId == -1) {
      List<GameEmulator> gameEmulators = frontendService.getVpxGameEmulators();
      for (GameEmulator gameEmulator : gameEmulators) {
        games.addAll(frontendService.getGamesByEmulator(gameEmulator.getId()));
      }
    }
    else {
      games.addAll(frontendService.getGamesByEmulator(emulatorId));
    }
    boolean killFrontend = false;
    for (Game game : games) {
      boolean newGame = applyGameDetails(game, false, false);
      if (newGame && !killFrontend) {
        LOG.info("New games have been found, automatically killing frontend to release locks.");
        frontendService.killFrontend();
        killFrontend = true;
      }
    }
    games.sort(Comparator.comparing(Game::getGameDisplayName));
    long duration = System.currentTimeMillis() - start;
    long avg = 0;
    if (!games.isEmpty()) {
      avg = duration / games.size();
    }
    LOG.info("Game fetch for emulator " + emulatorId + " took " + duration + "ms / " + games.size() + " games / " + avg + "ms avg.");
    return games;
  }

  public List<Game> getGamesByRom(int emulatorId, @NonNull String rom) {
    List<Game> games = frontendService.getGamesByEmulator(emulatorId);
    for (Game game : games) {
      applyGameDetails(game, false, false);
    }
    return games.stream()
        .filter(g ->
            (!StringUtils.isEmpty(g.getRom()) && g.getRom().equalsIgnoreCase(rom)) ||
                (!StringUtils.isEmpty(g.getTableName()) && g.getTableName().equalsIgnoreCase(rom)))
        .collect(Collectors.toList());
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

  public List<Integer> getGameIds() {
    return this.frontendService.getGameIds();
  }

  @SuppressWarnings("unused")
  @Nullable
  public synchronized Game getGame(int id) {
    Game game = frontendService.getGame(id);
    if (game != null) {
      applyGameDetails(game, false, true);
      return game;
    }
    return null;
  }

  /**
   * Returns the current highscore for the given game
   */
  @Nullable
  public ScoreSummary getScores(int gameId) {
    long serverId = preferencesService.getPreferenceValueLong(PreferenceNames.DISCORD_GUILD_ID, -1);
    Game game = getGame(gameId);
    if (game != null) {
      return highscoreService.getScoreSummary(serverId, game);
    }
    return null;
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

      Game rawGame = frontendService.getGame(version.getGameId());
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
      game = frontendService.getGame(gameId);
      if (game != null) {
        applyGameDetails(game, true, true);
        mameService.clearCacheFor(game.getRom());
        if (game.isVpxGame()) {
          highscoreService.scanScore(game, EventOrigin.USER_INITIATED);
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
  public HighscoreMetadata scanScore(int gameId, EventOrigin eventOrigin) {
    Game game = getGame(gameId);
    if (game != null) {
      return highscoreService.scanScore(game, eventOrigin);
    }
    return null;
  }

  public Game getGameByFilename(int emuId, String name) {
    Game game = this.frontendService.getGameByFilename(emuId, name);
    if (game != null) {
      //this will ensure that a scanned table is fetched
      game = this.getGame(game.getId());
    }
    return game;
  }

  public Game getGameByName(int emuId, String name) {
    Game game = this.frontendService.getGameByName(emuId, name);
    if (game != null) {
      //this will ensure that a scanned table is fetched
      game = this.getGame(game.getId());
    }
    return game;
  }

  private synchronized boolean applyGameDetails(@NonNull Game game, boolean forceScan, boolean forceScoreScan) {
    GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
    boolean newGame = (gameDetails == null);

    TableDetails tableDetails = null;
    if (gameDetails == null || forceScan) {

      if (gameDetails == null) {
        gameDetails = new GameDetails();
        gameDetails.setCreatedAt(new java.util.Date());
      }

      tableDetails = frontendService.getTableDetails(game.getId());

      if (game.isVpxGame()) {
        ScanResult scanResult = romService.scanGameFile(game);
        //always prefer PinUP Popper ROM name over the scanned value
        String scannedRomName = scanResult.getRom();
        String scannedTableName = scanResult.getTableName();

        if (tableDetails != null && StringUtils.isEmpty(scannedRomName) && !StringUtils.isEmpty(tableDetails.getRomName())) {
          scannedRomName = tableDetails.getRomName();
        }

        if (tableDetails != null && StringUtils.isEmpty(scannedTableName) && !StringUtils.isEmpty(tableDetails.getRomAlt())) {
          scannedTableName = tableDetails.getRomAlt();
        }
        gameDetails.setFoundControllerStop(scanResult.isFoundControllerStop());
        gameDetails.setFoundTableExit(scanResult.isFoundTableExit());
        gameDetails.setRomName(scannedRomName);
        gameDetails.setTableName(scannedTableName);
        gameDetails.setNvOffset(scanResult.getNvOffset());
        gameDetails.setHsFileName(scanResult.getHsFileName());

        gameDetails.setPupPack(scanResult.getPupPackName());
        gameDetails.setAssets(StringUtils.join(scanResult.getAssets(), ","));
      }
      else {
        if (tableDetails != null) {
          gameDetails.setRomName(tableDetails.getRomName());
          gameDetails.setTableName(tableDetails.getRomAlt());
        }
      }

      gameDetails.setPupId(game.getId());
      gameDetails.setUpdatedAt(new java.util.Date());

      gameDetailsRepository.saveAndFlush(gameDetails);
      LOG.info("Created GameDetails for " + game.getGameDisplayName() + ", was forced: " + forceScan);
    }

    GameEmulator emulator = game.getEmulator();
    if (emulator.isVpxEmulator() && emulator.getExe().exists()) {
      game.setLauncher(emulator.getExe().getName());
    }

    //apply the alt launcher exe as actually used one
    if (!StringUtils.isEmpty(game.getAltLauncherExe()) && game.getAltLauncherExe().contains(".exe")) {
      game.setLauncher(game.getAltLauncherExe());
    }

    //only apply legacy table name if the frontend fields are empty
    if (StringUtils.isEmpty(game.getTableName())) {
      game.setTableName(gameDetails.getTableName());
    }

    if (StringUtils.isEmpty(game.getRom())) {
      game.setRom(gameDetails.getRomName());
    }

    if (game.getDateAdded() == null) {
      game.setDateAdded(gameDetails.getCreatedAt());
    }
    if (game.getDateUpdated() == null) {
      game.setDateUpdated(gameDetails.getUpdatedAt());
    }

    //check alias
    String originalRom = mameRomAliasService.getRomForAlias(game.getEmulator(), game.getRom());
    if (!StringUtils.isEmpty(originalRom)) {
      String aliasName = game.getRom();
      game.setRom(originalRom);
      game.setRomAlias(aliasName);
    }

    // fill scanned values
    game.setScannedRom(gameDetails.getRomName());
    game.setScannedHsFileName(gameDetails.getHsFileName());
    game.setScannedAltRom(gameDetails.getTableName());
    game.setFoundControllerStop(gameDetails.getFoundControllerStop() != null ? gameDetails.getFoundControllerStop() : true);
    game.setFoundTableExit(gameDetails.getFoundTableExit() != null ? gameDetails.getFoundTableExit() : true);

    game.setNvOffset(gameDetails.getNvOffset());
    game.setCardDisabled(gameDetails.isCardsDisabled() != null && gameDetails.isCardsDisabled());

    game.setEventLogAvailable(gameDetails.getEventLog() != null);

    //only apply legacy highscore name if the Popper fields are empty
    if (StringUtils.isEmpty(game.getHsFileName())) {
      game.setHsFileName(gameDetails.getHsFileName());
    }

    // Only apply VPS data if the frontend does not provide them
    if (StringUtils.isEmpty(game.getExtTableId())) {
      game.setExtTableId(gameDetails.getExtTableId());
    }
    if (StringUtils.isEmpty(game.getExtTableVersionId())) {
      game.setExtTableVersionId(gameDetails.getExtTableVersionId());
    }
    if (StringUtils.isEmpty(game.getVersion())) {
      game.setVersion(gameDetails.getTableVersion());
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

    File rawDefaultPicture = defaultPictureService.getRawDefaultPicture(game);
    game.setDefaultBackgroundAvailable(rawDefaultPicture != null && rawDefaultPicture.exists());

    String updates = gameDetails.getUpdates();
    game.setVpsUpdates(VPSChanges.fromJson(updates));
    vpsService.applyVersionInfo(game);

    if (game.isVpxGame()) {
      Optional<Highscore> highscore = this.highscoreService.getHighscore(game, forceScoreScan, EventOrigin.USER_INITIATED);
      highscore.ifPresent(value -> game.setHighscoreType(value.getType() != null ? HighscoreType.valueOf(value.getType()) : null));
    }

    //run validations at the end!!!
    List<ValidationState> validate = gameValidationService.validate(game, true);
    game.setHasMissingAssets(gameValidationService.hasMissingAssets(validate));
    game.setHasOtherIssues(gameValidationService.hasOtherIssues(validate));

    if (validate.isEmpty()) {
      validate.add(GameValidationStateFactory.empty());
    }
    game.setValidationState(validate.get(0));

    GameScoreValidation scoreValidation = gameValidationService.validateHighscoreStatus(game, gameDetails, tableDetails);
    game.setValidScoreConfiguration(scoreValidation.isValidScoreConfiguration());

    return newGame;
  }

  public GameList getImportableTables(int emuId) {
    GameEmulator emulator = frontendService.getGameEmulator(emuId);
    if (emulator == null) {
      LOG.warn("No emulator found for id " + emuId);
      return new GameList();
    }

    GameList list = new GameList();
    File vpxTablesFolder = emulator.getTablesFolder();

    List<File> files = new ArrayList<>();
    if (emulator.isVpxEmulator()) {
      files.addAll(FileUtils.listFiles(vpxTablesFolder, new String[]{"vpx"}, true));
    }
    else if (emulator.isFpEmulator()) {
      files.addAll(FileUtils.listFiles(vpxTablesFolder, new String[]{"fpt"}, true));
    }

    List<Game> games = frontendService.getGamesByEmulator(emulator.getId());
    List<String> emulatorGameFileNames = games.stream().map(Game::getGameFileName).collect(Collectors.toList());
    for (File file : files) {
      String gameFileName = emulator.getGameFileName(file);
      if (!emulatorGameFileNames.contains(gameFileName)) {
        GameListItem item = new GameListItem();
        item.setName(file.getName());
        item.setFileName(file.getAbsolutePath());
        item.setEmuId(emulator.getId());
        list.getItems().add(item);
      }
    }
    Collections.sort(list.getItems(), Comparator.comparing(o -> o.getName().toLowerCase()));
    return list;
  }

  public Game getGameByTableParameter(String table) {
    File tableFile = new File(table.trim());

    // derive the emulator from the table folder
    int emuId = -1;
    for (GameEmulator emu : frontendService.getGameEmulators()) {
      if (StringUtils.startsWithIgnoreCase(tableFile.getAbsolutePath(), emu.getTablesDirectory())) {
        emuId = emu.getId();
        break;
      }
    }

    Game game = getGameByFilename(emuId, tableFile.getName());
    if (game == null && tableFile.getParentFile() != null) {
      game = getGameByFilename(emuId, tableFile.getParentFile().getName() + "\\" + tableFile.getName());
    }
    LOG.info("Resource Game Event Handler resolved \"" + game + "\" for table name \"" + table + "\"");
    return game;
  }

  public List<ValidationState> validate(Game game) {
    return gameValidationService.validate(game, false);
  }

  public synchronized Game save(Game game) throws Exception {
    GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
    gameDetails.setTemplateId(game.getTemplateId());
    gameDetails.setNotes(game.getNotes());
    gameDetails.setCardsDisabled(game.isCardDisabled());
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

  public synchronized void saveEventLog(HighscoreEventLog log) {
    try {
      GameDetails gameDetails = gameDetailsRepository.findByPupId(log.getGameId());
      gameDetails.setEventLog(log.toJson());
      gameDetailsRepository.saveAndFlush(gameDetails);
      LOG.info("Saved event log for " + log.getGameId());
    }
    catch (Exception e) {
      LOG.error("Failed to save event log: " + e.getMessage(), e);
    }
  }

  public boolean vpsLink(int gameId, String extTableId, String extTableVersionId) {
    GameDetails gameDetails = gameDetailsRepository.findByPupId(gameId);
    gameDetails.setExtTableId(extTableId);
    gameDetails.setExtTableVersionId(extTableVersionId);
    gameDetailsRepository.saveAndFlush(gameDetails);
    LOG.info("Linked game " + gameId + " to " + extTableId);

    // update the table in the frontend
    frontendService.vpsLink(gameId, extTableId, extTableVersionId);
    return true;
  }

  public boolean fixVersion(int gameId, String version, boolean overwrite) {
    GameDetails gameDetails = gameDetailsRepository.findByPupId(gameId);
    if (overwrite || StringUtils.isEmpty(gameDetails.getTableVersion())) {
      gameDetails.setTableVersion(version);
      gameDetailsRepository.saveAndFlush(gameDetails);
      LOG.info("Version saved for " + gameId + " to " + version);
      return true;
    }
    return false;
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

  public void resetUpdate(String rom, VpsDiffTypes diffType) {
    try {
      if (!StringUtils.isEmpty(rom)) {
        List<GameDetails> byRomName = gameDetailsRepository.findByRomName(rom);
        for (GameDetails gameDetails : byRomName) {
          String updates = gameDetails.getUpdates();
          if (updates != null) {
            List<String> existingUpdates = new ArrayList<>(Arrays.asList(updates.split(",")));
            existingUpdates.remove(diffType.name());
            updates = String.join(",", existingUpdates);
            gameDetails.setUpdates(updates);
            gameDetailsRepository.saveAndFlush(gameDetails);
            LOG.info("Resetted updates for " + gameDetails.getPupId() + " and removed \"" + diffType + "\", new update list: \"" + updates.trim() + "\"");
          }
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to reset update flag for rom '" + rom + "': " + e.getMessage(), e);
    }
  }

  public HighscoreFiles getHighscoreFiles(int id) {
    Game game = getGame(id);
    if (game.isVpxGame()) {
      return highscoreService.getHighscoreFiles(game);
    }
    return new HighscoreFiles();
  }

  public HighscoreEventLog getEventLog(int id) {
    try {
      Game game = getGame(id);
      GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
      String log = gameDetails.getEventLog();
      if (log != null) {
        return HighscoreEventLog.fromJson(HighscoreEventLog.class, log);
      }
    }
    catch (Exception e) {
      LOG.error("Failed to read event log: " + e.getMessage(), e);
    }
    return null;
  }

  public GameScoreValidation getGameScoreValidation(int id) {
    Game game = getGame(id);
    GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
    TableDetails tableDetails = frontendService.getTableDetails(id);
    return gameValidationService.validateHighscoreStatus(game, gameDetails, tableDetails);
  }

  public Game findMatch(String term) {
    List<Game> knownGames = getKnownGames(-1);
    double match = 1;
    Game tableMatch = null;
    for (Game knownGame : knownGames) {
      String displayName = knownGame.getGameDisplayName();
      double similarity = StringSimilarity.getSimilarity(displayName, term);
      if (similarity < match) {
        match = similarity;
        tableMatch = knownGame;
      }
    }

    if (match <= MATCHING_THRESHOLD) {
      LOG.info("Found matching table '" + tableMatch.getGameDisplayName() + "' with matching value of '" + match + "' for term '" + term + "'");
      return tableMatch;
    }
    LOG.info("Closed table match '" + tableMatch.getGameDisplayName() + "' with value '" + match + "' not sufficient for term '" + term + "'");
    return null;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    try {
      vpsService.update(this.getKnownGames(-1));
    }
    catch (Exception e) {
      LOG.error("Error initializing GameService: " + e.getMessage(), e);
    }
  }
}
