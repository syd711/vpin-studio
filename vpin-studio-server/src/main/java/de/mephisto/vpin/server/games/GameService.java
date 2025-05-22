package de.mephisto.vpin.server.games;

import de.mephisto.vpin.commons.utils.StringSimilarity;
import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameList;
import de.mephisto.vpin.restclient.games.GameListItem;
import de.mephisto.vpin.restclient.games.GameScoreValidation;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.highscores.logging.HighscoreEventLog;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.competitions.ScoreSummary;
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
import de.mephisto.vpin.server.vps.VpsService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


@Order(10)
@Service
public class GameService implements InitializingBean, ApplicationListener<ApplicationReadyEvent>, PreferenceChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(GameService.class);

  private static final double MATCHING_THRESHOLD = 0.1;

  @Autowired
  private FrontendService frontendService;

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
  private EmulatorService emulatorService;

  @Autowired
  private GameCachingService gameCachingService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  private ServerSettings serverSettings;

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
    return getGameByVpsTable(knownGames, vpsTableId, vpsTableVersionId);
  }


  public List<Game> getGamesByVpsTableId(@NonNull String vpsTableId, @Nullable String vpsTableVersionId) {
    List<Game> knownGames = getKnownGames(-1);
    return getGamesByVpsTableId(knownGames, vpsTableId, vpsTableVersionId);
  }

  public List<Game> getGamesByVpsTableId(@NonNull List<Game> knownGames, @NonNull String vpsTableId, @Nullable String vpsTableVersionId) {
    List<Game> matches = new ArrayList<>();
    for (Game game : knownGames) {
      if (!StringUtils.isEmpty(game.getExtTableId()) && game.getExtTableId().equals(vpsTableId)) {
        if (vpsTableVersionId == null) {
          matches.add(game);
          continue;
        }

        if (!StringUtils.isEmpty(game.getExtTableVersionId()) && game.getExtTableVersionId().equals(vpsTableVersionId)) {
          matches.add(game);
          continue;
        }
      }
    }
    return matches;
  }

  public Game getGameByVpsTable(@NonNull List<Game> knownGames, @NonNull String vpsTableId, @Nullable String vpsTableVersionId) {
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
    List<GameEmulator> emulators = emulatorService.getValidGameEmulators();
    mameRomAliasService.clearCache(emulators);
    highscoreService.refreshAvailableScores();
    gameCachingService.clearCache();
    getKnownGames(-1);
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
    return gameCachingService.getKnownGames(emulatorId);
  }

  public List<Game> getGamesByRom(int emulatorId, @NonNull String rom) {
    return gameCachingService.getGamesByRom(emulatorId, rom);
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

  @Nullable
  public Game getGame(int id) {
    return gameCachingService.getGame(id);
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
    return gameCachingService.scanGame(gameId);
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
    gameLifecycleService.notifyGameUpdated(game.getId());
    return getGame(game.getId());
  }

  public synchronized void saveEventLog(HighscoreEventLog log) {
    try {
      GameDetails gameDetails = gameDetailsRepository.findByPupId(log.getGameId());
      gameDetails.setEventLog(log.toJson());
      gameDetailsRepository.saveAndFlush(gameDetails);
      gameLifecycleService.notifyGameUpdated(log.getGameId());
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
    gameLifecycleService.notifyGameUpdated(gameId);
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
      gameLifecycleService.notifyGameUpdated(gameId);
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
        gameLifecycleService.notifyGameUpdated(gameId);
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
            gameLifecycleService.notifyGameUpdated(gameDetails.getPupId());
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

    if (tableMatch != null && match <= MATCHING_THRESHOLD) {
      LOG.info("Found matching table '" + tableMatch.getGameDisplayName() + "' with matching value of '" + match + "' for term '" + term + "'");
      return tableMatch;
    }
    if (tableMatch != null) {
      LOG.info("Closed table match '" + tableMatch.getGameDisplayName() + "' with value '" + match + "' not sufficient for term '" + term + "'");
    }
    else {
      LOG.info("No match for term '" + term + "'");
    }
    return null;
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (propertyName.equals(PreferenceNames.SERVER_SETTINGS)) {
      serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
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

  public boolean clearMameCaches() {
    List<Game> games = getKnownGames(-1);
    vpsService.update(games);
    boolean result = mameService.clearGamesCache(games);

    List<GameEmulator> gameEmulators = emulatorService.getValidGameEmulators();
    result &= mameService.clearValidationsCache(gameEmulators);
    result &= mameRomAliasService.clearCache(gameEmulators);
    return result;
  }

  public boolean clearMameCacheFor(String rom) {
    List<GameEmulator> gameEmulators = emulatorService.getValidGameEmulators();
    mameRomAliasService.clearCache(gameEmulators);
    return mameService.clearCacheFor(rom);
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    //ALWAYS AVOID CALLING GETKNOWNGAMES DURING THE INITILIZATION PHASE OF THE SERVER
    List<Integer> unknownGames = getUnknownGames();
    if (unknownGames.isEmpty()) {
      clearMameCaches();
    }
  }
}
