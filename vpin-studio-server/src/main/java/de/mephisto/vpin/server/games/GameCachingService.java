package de.mephisto.vpin.server.games;

import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.dmd.DMDPackageTypes;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.games.GameScoreValidation;
import de.mephisto.vpin.restclient.games.ValidationStateFactory;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.preferences.ServerSettings;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionChangeListener;
import de.mephisto.vpin.server.competitions.CompetitionLifecycleService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.emulators.EmulatorChangeListener;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreChangeEvent;
import de.mephisto.vpin.server.highscores.HighscoreChangeListener;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.listeners.EventOrigin;
import de.mephisto.vpin.server.mame.MameRomAliasService;
import de.mephisto.vpin.server.mame.MameService;
import de.mephisto.vpin.server.players.Player;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GameCachingService implements InitializingBean, PreferenceChangedListener, GameLifecycleListener, GameDataChangedListener, CompetitionChangeListener, HighscoreChangeListener, EmulatorChangeListener {
  private final static Logger LOG = LoggerFactory.getLogger(GameCachingService.class);

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private GameDetailsRepository gameDetailsRepository;

  @Autowired
  private MameRomAliasService mameRomAliasService;

  @Autowired
  private BackglassService backglassService;

  @Autowired
  private VpsService vpsService;

  @Autowired
  private GameValidationService gameValidationService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private AltColorService altColorService;

  @Autowired
  private DefaultPictureService defaultPictureService;

  @Autowired
  private PupPacksService pupPacksService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private MameService mameService;

  @Autowired
  private RomService romService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  @Autowired
  private CompetitionLifecycleService competitionLifecycleService;

  private ServerSettings serverSettings;

  private final Map<Integer, List<Game>> allGamesByEmulatorId = new ConcurrentHashMap<>();

  public void clearCache() {
    allGamesByEmulatorId.clear();
  }

  public void clearCacheForEmulator(int emulatorId) {
    allGamesByEmulatorId.remove(emulatorId);
  }

  public Game invalidate(int gameId) {
    Game game = getGame(gameId);
    if (game != null) {
      List<Game> games = allGamesByEmulatorId.computeIfAbsent(game.getEmulatorId(), k -> new ArrayList<>());
      games.remove(game);
//      LOG.info("-------------------> Evicted {}", game.getGameDisplayName());
    }
    return getGame(gameId);
  }

  public void invalidateByRom(int emulatorId, @NonNull String rom) {
    List<Game> games = allGamesByEmulatorId.computeIfAbsent(emulatorId, k -> new ArrayList<>());
    for (Game game : new ArrayList<>(games)) {
      if (rom.trim().equals(game.getRom()) || rom.trim().equals(game.getRomAlias())) {
        games.remove(game);
        getGame(game.getId());
//        LOG.info("-------------------> Evicted {}", game.getGameDisplayName());
      }
    }
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
        invalidate(gameId);
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
  public Game getGame(int id) {
    Game game = getGameCached(id);
    if (game == null) {
      game = frontendService.getOriginalGame(id);
      if (game != null && game.getEmulator() != null) {
        applyGameDetails(game, false, true);
        List<Game> games = allGamesByEmulatorId.computeIfAbsent(game.getEmulatorId(), k -> new ArrayList<>());
        games.add(game);
      }
    }
    return game;
  }

  private Game getGameCached(int id) {
    Collection<List<Game>> values = allGamesByEmulatorId.values();
    for (List<Game> value : values) {
      for (Game game : value) {
        if (game.getId() == id) {
          return game;
        }
      }
    }
    return null;
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

  @SuppressWarnings("unused")
  public List<Game> getKnownGames(int emulatorId) {
    List<Game> games = new ArrayList<>();
    if (emulatorId == -1) {
      games.addAll(getVpxGames());
    }
    else {
      GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
      if (emulator != null && emulator.isEnabled()) {
        if (!allGamesByEmulatorId.containsKey(emulator.getId())) {
          fetchEmulatorGames(emulator);
        }
        games.addAll(allGamesByEmulatorId.get(emulator.getId()));
      }
    }

    games = games.stream().filter(g -> g.getEmulator() != null).collect(Collectors.toList());
    games.sort(Comparator.comparing(o -> o.getGameDisplayName().toLowerCase()));
    return games;
  }

  private List<Game> getVpxGames() {
    List<Game> games = new ArrayList<>();
    List<GameEmulator> gameEmulators = emulatorService.getVpxGameEmulators();
    for (GameEmulator gameEmulator : gameEmulators) {
      if (gameEmulator.isEnabled()) {
        if (!allGamesByEmulatorId.containsKey(gameEmulator.getId())) {
          fetchEmulatorGames(gameEmulator);
        }
        games.addAll(allGamesByEmulatorId.get(gameEmulator.getId()));
      }
    }
    return games;
  }

  private void fetchEmulatorGames(GameEmulator emulator) {
    long start = System.currentTimeMillis();
    List<Game> gamesByEmulator = frontendService.getGamesByEmulator(emulator.getId());
    boolean killFrontend = false;
    for (Game game : gamesByEmulator) {
      boolean newGame = applyGameDetails(game, false, false);
      if (newGame) {
        gameLifecycleService.notifyGameCreated(game.getId());
        highscoreService.scanScore(game, EventOrigin.INITIAL_SCAN);
      }

      if (newGame && !killFrontend) {
        LOG.info("New games have been found, automatically killing frontend to release locks.");
        frontendService.killFrontend();
        killFrontend = true;
      }
    }
    allGamesByEmulatorId.put(emulator.getId(), gamesByEmulator);
    long duration = System.currentTimeMillis() - start;
    long avg = 0;
    if (!gamesByEmulator.isEmpty()) {
      avg = duration / gamesByEmulator.size();
    }
    LOG.info("Game fetch for emulator " + emulator.getName() + " took " + duration + "ms / " + gamesByEmulator.size() + " games / " + avg + "ms avg.");
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

        gameDetails.setDMDType(scanResult.getDMDType() != null ? DMDPackageTypes.valueOf(scanResult.getDMDType()) : DMDPackageTypes.Standard);
        gameDetails.setDMDGameName(scanResult.getDMDGameName());
        gameDetails.setDMDProjectFolder(scanResult.getDMDProjectFolder());

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

    game.setDMDType(gameDetails.getDMDType());
    game.setDMDGameName(gameDetails.getDMDGameName());
    game.setDMDProjectFolder(gameDetails.getDMDProjectFolder());

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
    PupPack pupPack = pupPacksService.getPupPackCached(game);
    if (pupPack != null) {
      game.setPupPackName(pupPack.getName());
    }

    game.setIgnoredValidations(ValidationState.toIds(gameDetails.getIgnoredValidations()));
    game.setAltSoundAvailable(altSoundService.isAltSoundAvailable(game));
    game.setAltColorType(altColorService.getAltColorType(game));

    File rawDefaultPicture = defaultPictureService.getRawDefaultPicture(game);
    game.setDefaultBackgroundAvailable(rawDefaultPicture.exists());

    DirectB2S b2s = backglassService.getDirectB2SAndVersions(game);
    game.setNbDirectB2S(b2s != null ? b2s.getNbVersions() : -1);

    String updates = gameDetails.getUpdates();
    game.setVpsUpdates(VPSChanges.fromJson(updates));
    vpsService.applyVersionInfo(game);

    //do not parse highscore and generate highscore cards for new games, causing concurrent DB access likely through the FX thread
    if (game.isVpxGame()) {
      if (!newGame) {
        Optional<Highscore> highscore = this.highscoreService.getHighscore(game, forceScoreScan, EventOrigin.USER_INITIATED);
        highscore.ifPresent(value -> game.setHighscoreType(value.getType() != null ? HighscoreType.valueOf(value.getType()) : null));
      }
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

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.SERVER_SETTINGS.equals(propertyName)) {
      serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);
    }
  }

  //---------- Game Lifecycle Listener ---------------------

  @Override
  public void gameCreated(int gameId) {
    invalidate(gameId);
  }

  @Override
  public void gameUpdated(int gameId) {
    invalidate(gameId);
  }

  @Override
  public void gameDeleted(int gameId) {
    Collection<List<Game>> values = allGamesByEmulatorId.values();
    for (List<Game> value : values) {
      Optional<Game> first = value.stream().filter(g -> g.getId() == gameId).findFirst();
      if (first.isPresent()) {
        value.remove(first.get());
        return;
      }
    }
  }

  //---------- GameDataChange Listener ---------------------

  @Override
  public void gameDataChanged(@NonNull GameDataChangedEvent changedEvent) {
    invalidate(changedEvent.getGameId());
  }

  @Override
  public void gameAssetChanged(@NonNull GameAssetChangedEvent changedEvent) {
    invalidate(changedEvent.getGameId());

    switch (changedEvent.getAssetType()) {
      case ROM:
      case ALT_COLOR:
      case PUP_PACK:
      case ALT_SOUND: {
        List<Game> vpxGames = getVpxGames();
        if (changedEvent.getAsset() != null) {
          Object asset = changedEvent.getAsset();
          if (asset instanceof String) {
            String rom = String.valueOf(asset);
            for (Game vpxGame : vpxGames) {
              if (rom.equalsIgnoreCase(vpxGame.getRom()) || rom.equalsIgnoreCase(vpxGame.getTableName())) {
                invalidate(vpxGame.getId());
              }
            }
          }
        }
        return;
      }
      case INI:
      case POV:
      case DIRECTB2S: {
        Object asset = changedEvent.getAsset();
        if (asset instanceof String) {
          String filename = String.valueOf(asset);
          String fileAssetBaseName = FilenameUtils.getBaseName(filename);
          List<Game> knownGames = getKnownGames(-1);
          for (Game knownGame : knownGames) {
            String gameBaseName = FilenameUtils.getBaseName(knownGame.getGameFileName());
            if (gameBaseName.equalsIgnoreCase(fileAssetBaseName)) {
              invalidate(knownGame.getId());
            }
          }
        }
      }
      default: {
//        LOG.warn("Unhandled asset change event found: {}", changedEvent.getAsset());
      }
    }
  }
  //---------- Competition Change Listener ---------------------

  @Override
  public void competitionStarted(@NonNull Competition competition) {
    invalidate(competition.getGameId());
  }

  @Override
  public void competitionCreated(@NonNull Competition competition) {
    invalidate(competition.getGameId());
  }

  @Override
  public void competitionChanged(@NonNull Competition competition) {
    invalidate(competition.getGameId());
  }

  @Override
  public void competitionFinished(@NonNull Competition competition, @Nullable Player winner, @NonNull ScoreSummary scoreSummary) {
    invalidate(competition.getGameId());
  }

  @Override
  public void competitionDeleted(@NonNull Competition competition) {
    invalidate(competition.getGameId());
  }

  //---------- Highscore Change Listener ---------------------

  @Override
  public void highscoreChanged(@NotNull HighscoreChangeEvent event) {
    invalidate(event.getGame().getId());
  }

  @Override
  public void highscoreUpdated(@NotNull Game game, @NotNull Highscore highscore) {
    invalidate(game.getId());
  }

  //---------- Emulator Change Listener ---------------------

  @Override
  public void emulatorChanged(int emulatorId) {
    clearCacheForEmulator(emulatorId);
  }

  //---------- InitializingBean----------------------------------

  @Override
  public void afterPropertiesSet() throws Exception {
    mameRomAliasService.setGameCachingService(this);

    serverSettings = preferencesService.getJsonPreference(PreferenceNames.SERVER_SETTINGS, ServerSettings.class);

    preferencesService.addChangeListener(this);
    gameLifecycleService.addGameLifecycleListener(this);
    gameLifecycleService.addGameDataChangedListener(this);
    competitionLifecycleService.addCompetitionChangeListener(this);
    emulatorService.addEmulatorChangeListener(this);
  }
}
