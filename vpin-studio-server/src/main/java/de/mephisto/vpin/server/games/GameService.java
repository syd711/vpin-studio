package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.StringSimilarity;
import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.directb2s.DirectB2SAndVersions;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.games.GameScoreValidation;
import de.mephisto.vpin.restclient.games.ValidationStateFactory;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.highscores.logging.HighscoreEventLog;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.highscores.*;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.mame.MameRomAliasService;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.players.PlayerService;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GameService implements InitializingBean, ApplicationListener<ApplicationReadyEvent>, PreferenceChangedListener {
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
  private BackglassService backglassService;

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

  @Autowired
  private EmulatorService emulatorService;

  private ServerSettings serverSettings;

  private final List<GameLifecycleListener> lifecycleListeners = new ArrayList<>();
  private final List<GameDataChangedListener> gameDataChangedListeners = new ArrayList<>();

  /**
   * the refresh timer to keep VPS updated
   */
  private Timer refreshTimer;

  @Value("${vps.refreshInterval:2}")
  private int refreshInterval;

  /**
   * Public API endpoint to fetch all games
   *
   * @return
   */
  public List<Game> getGames() {
    long start = System.currentTimeMillis();
    List<Game> games = getKnownGames(-1);
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
    emulatorService.loadEmulators();
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
      List<GameEmulator> gameEmulators = emulatorService.getVpxGameEmulators();
      for (GameEmulator gameEmulator : gameEmulators) {
        games.addAll(frontendService.getGamesByEmulator(gameEmulator.getId()));
      }
    }
    else {
      games.addAll(frontendService.getGamesByEmulator(emulatorId));
    }

    games = games.stream().filter(g -> g.getEmulator() != null).collect(Collectors.toList());
    boolean killFrontend = false;
    for (Game game : games) {
      boolean newGame = applyGameDetails(game, false, false);
      if (newGame) {
        notifyGameCreated(game);
        scanScore(game.getId(), EventOrigin.INITIAL_SCAN);
      }

      if (newGame && !killFrontend) {
        LOG.info("New games have been found, automatically killing frontend to release locks.");
        frontendService.killFrontend();
        killFrontend = true;
      }
    }
    games.sort(Comparator.comparing(o -> o.getGameDisplayName().toLowerCase()));
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

  public boolean resetGame(int gameId, long score) {
    Game game = this.getGame(gameId);
    if (game == null) {
      return false;
    }


    if (highscoreBackupService.backup(game)) {
      return highscoreService.resetHighscore(game, score);
    }

    return false;
  }

  public List<Integer> getGameIds() {
    return this.frontendService.getGameIds();
  }

  @SuppressWarnings("unused")
  @Nullable
  public Game getGame(int id) {
    if (id >= 0) {
      Game game = frontendService.getOriginalGame(id);
      if (game != null && game.getEmulator() != null) {
        applyGameDetails(game, false, true);
        return game;
      }
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

      Game rawGame = frontendService.getOriginalGame(version.getGameId());
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
      game = frontendService.getOriginalGame(gameId);
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

  public Game getGameByBaseFilename(int emuId, String baseFilename) {
    Game game = this.frontendService.getGameByBaseFilename(emuId, baseFilename);
    if (game != null) {
      //this will ensure that a scanned table is fetched
      game = this.getGame(game.getId());
    }
    return game;
  }

  public Game getGameByFilename(int emuId, String filename) {
    Game game = this.frontendService.getGameByFilename(emuId, filename);
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

  public Game getGameByDirectB2S(int emuId, String filename) {
    String basefileName = de.mephisto.vpin.restclient.util.FileUtils.baseUniqueFile(filename);
    return getGameByBaseFilename(emuId, basefileName);
  }

  private boolean applyGameDetails(@NonNull Game game, boolean forceScan, boolean forceScoreScan) {
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
        String scannedRomName = scanResult.getRom();
        String scannedTableName = scanResult.getTableName();

        //always prefer PinUP Popper ROM name over the scanned value
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
        gameDetails.setVrRoomEnabled(!scanResult.isVrRoomDisabled());
        gameDetails.setVrRoomSupport(scanResult.isVrRoomSupport());

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
    if (emulator != null && emulator.isVpxEmulator() && emulator.getExe() != null && emulator.getExe().exists()) {
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
    if (game.getEmulator() != null) {
      String originalRom = mameRomAliasService.getRomForAlias(game.getEmulator(), game.getRom());
      if (!StringUtils.isEmpty(originalRom)) {
        String aliasName = game.getRom();
        game.setRom(originalRom);
        game.setRomAlias(aliasName);
      }
    }

    // fill scanned values
    game.setScannedRom(gameDetails.getRomName());
    game.setScannedHsFileName(gameDetails.getHsFileName());
    game.setScannedAltRom(gameDetails.getTableName());
    game.setVrRoomEnabled(gameDetails.getVrRoomEnabled() != null ? gameDetails.getVrRoomEnabled() : false);
    game.setVrRoomSupport(gameDetails.getVrRoomSupport() != null ? gameDetails.getVrRoomSupport() : false);
    game.setFoundControllerStop(gameDetails.getFoundControllerStop() != null ? gameDetails.getFoundControllerStop() : true);
    game.setFoundTableExit(gameDetails.getFoundTableExit() != null ? gameDetails.getFoundTableExit() : true);

    game.setNvOffset(gameDetails.getNvOffset());
    game.setCardDisabled(gameDetails.isCardsDisabled() != null && gameDetails.isCardsDisabled());

    game.setEventLogAvailable(gameDetails.getEventLog() != null);

    //only apply legacy highscore name if the Popper fields are empty
    if (StringUtils.isEmpty(game.getHsFileName())) {
      game.setHsFileName(gameDetails.getHsFileName());
    }
    else {
      //TODO this smells. The TableDetails are only loaded in case the game has no highscore filename, because this one is mapped to a custom field
      if (tableDetails == null) {
        tableDetails = frontendService.getTableDetails(game.getId());
      }
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
    game.setComment(gameDetails.getNotes());

    //PUP pack assignment: we have to differ between the scanned name and the actual resolved one which could be different.
    game.setPupPackName(gameDetails.getPupPack());
    PupPack pupPack = pupPackService.getPupPackCached(game);
    if (pupPack != null) {
      game.setPupPack(pupPack);
      game.setPupPackName(pupPack.getName());
    }

    game.setIgnoredValidations(ValidationState.toIds(gameDetails.getIgnoredValidations()));
    game.setAltSoundAvailable(altSoundService.isAltSoundAvailable(game));
    game.setAltColorType(altColorService.getAltColorType(game));

    File rawDefaultPicture = defaultPictureService.getRawDefaultPicture(game);
    game.setDefaultBackgroundAvailable(rawDefaultPicture.exists());

    DirectB2SAndVersions b2s = backglassService.getDirectB2SAndVersions(game);
    game.setNbDirectB2S(b2s != null ? b2s.getNbVersions() : -1);

    String updates = gameDetails.getUpdates();
    game.setVpsUpdates(VPSChanges.fromJson(updates));
    vpsService.applyVersionInfo(game);

    //do not parse highscore and generate highscore cards for new games, causing concurrent DB access likely through the FX thread
    if (game.isVpxGame() && !newGame) {
      Optional<Highscore> highscore = this.highscoreService.getHighscore(game, forceScoreScan, EventOrigin.USER_INITIATED);
      highscore.ifPresent(value -> game.setHighscoreType(value.getType() != null ? HighscoreType.valueOf(value.getType()) : null));
    }

    //run validations at the end!!!
    List<ValidationState> validate = gameValidationService.validate(game, true);
    game.setHasMissingAssets(gameValidationService.hasMissingAssets(validate));
    game.setHasOtherIssues(gameValidationService.hasOtherIssues(validate));

    if (validate.isEmpty()) {
      validate.add(ValidationStateFactory.empty());
    }
    game.setValidationState(validate.get(0));

    GameScoreValidation scoreValidation = gameValidationService.validateHighscoreStatus(game, gameDetails, tableDetails, frontendService.getFrontendType(), serverSettings);
    game.setValidScoreConfiguration(scoreValidation.isValidScoreConfiguration());

    return newGame;
  }

  public GameList getImportableTables(int emuId) {
    GameEmulator emulator = emulatorService.getGameEmulator(emuId);
    if (emulator == null) {
      LOG.warn("No emulator found for id " + emuId);
      return new GameList();
    }

    GameList list = new GameList();
    File vpxTablesFolder = emulator.getGamesFolder();

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

  public Game getGameByTableAndEmuParameter(@NonNull String table, @Nullable String emuDirOrName) {
    File tableFile = new File(table.trim());

    // derive the emulator from the name or folder
    List<GameEmulator> matchingEmulators = new ArrayList<>();
    if (!StringUtils.isEmpty(emuDirOrName)) {
      for (GameEmulator emu : emulatorService.getValidGameEmulators()) {
        if (!emu.isEnabled()) {
          continue;
        }

        if (emu.getInstallationFolder().getAbsolutePath().equals(emuDirOrName)) {
          matchingEmulators.add(emu);
          continue;
        }

        if (emu.getName() != null && emu.getName().equals(emuDirOrName)) {
          matchingEmulators.add(emu);
        }
      }
    }

    if (!StringUtils.isEmpty(emuDirOrName) && matchingEmulators.isEmpty()) {
      LOG.warn("No matching emulator found for emulator installation parameter \"{}\"", emuDirOrName);
    }

    if (matchingEmulators.isEmpty()) {
      // derive the emulator from the table folder
      for (GameEmulator emu : emulatorService.getValidGameEmulators()) {
        if (!emu.isEnabled()) {
          continue;
        }

        if (StringUtils.startsWithIgnoreCase(tableFile.getAbsolutePath(), emu.getGamesDirectory())) {
          matchingEmulators.add(emu);
          break;
        }
      }
    }

    Game game = null;
    int emuId = -1;
    for (GameEmulator matchingEmulator : matchingEmulators) {
      emuId = matchingEmulator.getId();
      game = getGameByFilename(emuId, tableFile.getName());
      if (game == null && tableFile.getParentFile() != null) {
        LOG.warn("No game found with name \"{}\" for emulator with id \"{}\"", table, emuId);
        game = getGameByFilename(emuId, tableFile.getParentFile().getName() + "\\" + tableFile.getName());
      }

      if (game != null) {
        break;
      }
    }


    LOG.info("Resource Game Event Handler resolved \"" + game + "\" for table name \"" + table + "\" from emulator {}", emuId);
    return game;
  }

  public List<ValidationState> validate(Game game) {
    return gameValidationService.validate(game, false);
  }

  public synchronized Game save(Game game) throws Exception {
    GameDetails gameDetails = gameDetailsRepository.findByPupId(game.getId());
    gameDetails.setTemplateId(game.getTemplateId());
    gameDetails.setNotes(game.getComment());
    gameDetails.setCardsDisabled(game.isCardDisabled());
    gameDetails.setIgnoredValidations(ValidationState.toIdString(game.getIgnoredValidations()));
    if (game.getVpsUpdates() != null) {
      VPSChanges vpsUpdates = game.getVpsUpdates();
      String json = vpsUpdates.toJson();
      gameDetails.setUpdates(json);
    }
    gameDetailsRepository.saveAndFlush(gameDetails);
    LOG.info("Saved \"" + game.getGameDisplayName() + "\"");
    notifyGameUpdated(game);
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
      LOG.error("Failed to save event log: {}", e.getMessage(), e);
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
      LOG.error("Failed to reset update flag for rom '{}': {}", rom, e.getMessage(), e);
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
    return gameValidationService.validateHighscoreStatus(game, gameDetails, tableDetails, frontendService.getFrontendType(), serverSettings);
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
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (propertyName.equals(PreferenceNames.SERVER_SETTINGS)) {
      serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    }
  }

  public void addGameLifecycleListener(@NonNull GameLifecycleListener lifecycleListener) {
    this.lifecycleListeners.add(lifecycleListener);
  }

  public void addGameDataChangedListener(@NonNull GameDataChangedListener listener) {
    this.gameDataChangedListeners.add(listener);
  }

  private void notifyGameCreated(@NonNull Game game) {
    for (GameLifecycleListener lifecycleListener : lifecycleListeners) {
      lifecycleListener.gameCreated(game);
    }
  }

  private void notifyGameUpdated(@NonNull Game game) {
    for (GameLifecycleListener lifecycleListener : lifecycleListeners) {
      lifecycleListener.gameUpdated(game);
    }
  }

  public void notifyGameDeleted(@NonNull Game game) {
    for (GameLifecycleListener lifecycleListener : lifecycleListeners) {
      lifecycleListener.gameDeleted(game);
    }
  }

  public void notifyGameDataChanged(@NonNull Game game, @NonNull TableDetails oldData, @NonNull TableDetails newData) {
    GameDataChangedEvent event = new GameDataChangedEvent(game, oldData, newData);
    for (GameDataChangedListener listener : gameDataChangedListeners) {
      listener.gameDataChanged(event);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferencesService.addChangeListener(this);
    preferenceChanged(PreferenceNames.SERVER_SETTINGS, null, null);
    try {
      highscoreService.setGameService(this);
    }
    catch (Exception e) {
      LOG.error("Error initializing GameService: " + e.getMessage(), e);
    }
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    mameService.setGameService(this);
    List<Integer> unknownGames = getUnknownGames();
    //ALWAYS AVOID CALLING GETKNOWNGAMES DURING THE INITILIZATION PHASE OF THE SERVER
    if (unknownGames.isEmpty()) {
      List<Game> games = getKnownGames(-1);
      vpsService.update(games);
      mameService.clearCache();
    }
  }
}
