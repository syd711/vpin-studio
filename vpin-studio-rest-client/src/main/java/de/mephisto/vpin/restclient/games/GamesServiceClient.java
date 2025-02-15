package de.mephisto.vpin.restclient.games;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.highscores.HighscoreMetadataRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreListRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.highscores.logging.HighscoreEventLog;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import de.mephisto.vpin.restclient.validation.ValidationState;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


/*********************************************************************************************************************
 * Games
 ********************************************************************************************************************/
public class GamesServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  private final Map<Integer, List<GameRepresentation>> allGames = new HashMap<>();
  /**
   * a status map to avoid multiple loads in parallel, check getGamesCached()
   */
  private final Map<Integer, Boolean> loadingFlags = new HashMap<>();
  private List<Integer> ignoredEmulatorIds = new ArrayList<>();

  public GamesServiceClient(VPinStudioClient client) {
    super(client);
  }

  public void setIgnoredEmulatorIds(List<Integer> ignoredEmulatorIds) {
    this.ignoredEmulatorIds = ignoredEmulatorIds;
  }

  public void clearCache() {
    this.allGames.clear();
    this.loadingFlags.clear();
  }

  public void clearCache(int emulatorId) {
    this.allGames.remove(emulatorId);
    this.loadingFlags.remove(emulatorId);
  }

  public void reload() {
    getRestClient().get(API + "games/reload", Boolean.class);
  }

  public void playGame(int id, String altExe) {
    try {
      Map<String, Object> params = new HashMap<>();
      params.put("altExe", altExe);
      getRestClient().put(API + "games/play/" + id, params);
    }
    catch (Exception e) {
      LOG.error("Failed to start game " + id + ": " + e.getMessage(), e);
    }
  }

  public UploadDescriptor uploadTable(File file, UploadType uploadType, int gameId, int emuId, FileUploadProgressListener listener) {
    try {
      String url = getRestClient().getBaseUrl() + API + "games/upload";
      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add("mode", uploadType.name());
      map.add("gameId", gameId);
      map.add("emuId", emuId);
      ResponseEntity<UploadDescriptor> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, createUpload(map, file, -1, null, AssetType.TABLE, listener), UploadDescriptor.class);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public UploadDescriptor proccessTableUpload(UploadDescriptor uploadDescriptor) throws Exception {
    try {
      return getRestClient().post(API + "games/process", uploadDescriptor, UploadDescriptor.class);
    }
    catch (Exception e) {
      LOG.error("Failed to process table: " + e.getMessage(), e);
      throw e;
    }
  }

  public void deleteGame(@NonNull DeleteDescriptor descriptor, @NonNull GameRepresentation game) {
    try {
      getRestClient().post(API + "games/delete", descriptor, Boolean.class);
      int emulatorId = game.getEmulatorId();
      List<GameRepresentation> gameRepresentations = this.allGames.get(emulatorId);
      gameRepresentations.remove(game);
    }
    catch (Exception e) {
      LOG.error("Failed to delete games " + descriptor.getGameIds() + ": " + e.getMessage(), e);
    }
  }

  public List<GameRepresentation> getGamesByRom(String rom) {
    List<GameRepresentation> gameList = this.getVpxGamesCached();
    List<GameRepresentation> result = new ArrayList<>();
    for (GameRepresentation gameRepresentation : gameList) {
      if ((!StringUtils.isEmpty(gameRepresentation.getRom()) && gameRepresentation.getRom().equalsIgnoreCase(rom)) ||
          (!StringUtils.isEmpty(gameRepresentation.getTableName()) && gameRepresentation.getTableName().equalsIgnoreCase(rom))) {
        result.add(gameRepresentation);
      }
    }
    return result;
  }

  public GameRepresentation getFirstGameByRom(String rom) {
    List<GameRepresentation> gamesCached = this.getGamesCached(-1);
    for (GameRepresentation gameRepresentation : gamesCached) {
      String gameRom = gameRepresentation.getRom();
      if (!StringUtils.isEmpty(gameRom) && gameRom.equalsIgnoreCase(rom)) {
        return gameRepresentation;
      }
    }
    return null;
  }


  public List<GameRepresentation> getGamesByFileName(int emuId, String gameFileName) {
    List<GameRepresentation> gameList = this.getGamesCached(emuId);
    List<GameRepresentation> result = new ArrayList<>();
    for (GameRepresentation gameRepresentation : gameList) {
      if (gameRepresentation.getGameFileName().trim().equalsIgnoreCase(gameFileName)) {
        result.add(gameRepresentation);
      }
    }
    return result;
  }

  public List<GameRepresentation> getGamesByGameName(String gameName) {
    List<GameRepresentation> gameList = this.getGamesCached(-1);
    List<GameRepresentation> result = new ArrayList<>();
    for (GameRepresentation gameRepresentation : gameList) {
      if (gameRepresentation.getGameName().equalsIgnoreCase(gameName)) {
        result.add(gameRepresentation);
      }
    }
    return result;
  }

  public List<ValidationState> getValidations(int gameId) {
    return Arrays.asList(getRestClient().get(API + "games/validations/" + gameId, ValidationState[].class));
  }

  public HighscoreEventLog getEventLog(int gameId) {
    return getRestClient().get(API + "games/eventlog/" + gameId, HighscoreEventLog.class);
  }

  //TODO there is no method to get a single cached game???
  public GameRepresentation getGameCached(int id) {
    Collection<List<GameRepresentation>> values = allGames.values();
    for (List<GameRepresentation> value : values) {
      for (GameRepresentation gameRepresentation : value) {
        if(gameRepresentation.getId() == id) {
          return gameRepresentation;
        }
      }
    }
    return getGame(id);
  }

  public GameRepresentation getGame(int id) {
    try {
      GameRepresentation gameRepresentation = getRestClient().get(API + "games/" + id, GameRepresentation.class);
      if (gameRepresentation != null && !this.allGames.isEmpty()) {
        int emulatorId = gameRepresentation.getEmulatorId();
        // get and from cache and possibly update the cache
        List<GameRepresentation> games = this.getGamesCached(emulatorId);
        int index = games.indexOf(gameRepresentation);
        if (index != -1) {
          games.remove(index);
          games.add(index, gameRepresentation);
        }
        else {
          games.add(gameRepresentation);
          games.sort(Comparator.comparing(GameRepresentation::getGameDisplayName));
        }
      }
      return gameRepresentation;
    }
    catch (Exception e) {
      LOG.error("Failed to retrieve game: " + e.getMessage());
    }
    return null;
  }

  public List<Integer> getUnknownGameIds() {
    try {
      List<Integer> unknowns = Arrays.asList(getRestClient().get(API + "games/unknowns", Integer[].class));
      if (!unknowns.isEmpty()) {
        this.clearCache();
      }
      return unknowns;
    }
    catch (Exception e) {
      LOG.error("Failed to read unknowns game ids: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  // private as getGamesCached() should be called instead
  private List<GameRepresentation> getKnownGames(int emulatorId) {
    try {
      List<GameRepresentation> emulatorGames = new ArrayList<>(Arrays.asList(getRestClient().get(API + "games/knowns/" + emulatorId, GameRepresentation[].class)));
      return emulatorGames;
    }
    catch (Exception e) {
      LOG.error("Failed to read known games: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  public List<VpsTable> getInstalledVpsTables() {
    List<VpsTable> vpsTables = new ArrayList<>();
    List<GameRepresentation> gamesCached = getGamesCached(-1);
    for (GameRepresentation game : gamesCached) {
      if (!StringUtils.isEmpty(game.getExtTableId())) {
        VpsTable tableById = client.getVpsService().getTableById(game.getExtTableId());
        if (tableById != null) {
          vpsTables.add(tableById);
        }
      }
    }
    return vpsTables;
  }

  @Nullable
  public GameRepresentation getGameByVpsTable(@NonNull VpsTable vpsTable, @Nullable VpsTableVersion vpsTableVersion) {
    return getGameByVpsTable(vpsTable.getId(), vpsTableVersion != null ? vpsTableVersion.getId() : null);
  }

  @Nullable
  public GameRepresentation getGameByVpsTable(@NonNull String vpsTableId, @Nullable String vpsTableVersionId) {
    List<GameRepresentation> gamesCached = getGamesCached(-1);
    GameRepresentation hit = null;
    for (GameRepresentation game : gamesCached) {
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

  public List<Integer> getGameIds() {
    try {
      final RestTemplate restTemplate = new RestTemplate();
      return Arrays.asList(restTemplate.getForObject(getRestClient().getBaseUrl() + API + "games/ids", Integer[].class));
    }
    catch (Exception e) {
      LOG.error("Failed to read game ids: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  public List<Integer> getGameIdsCached(int emuId) {
    return getGamesCached(emuId).stream().map(g -> g.getId()).collect(Collectors.toList());
  }

  public ScoreSummaryRepresentation getGameScores(int id) {
    try {
      return getRestClient().get(API + "games/scores/" + id, ScoreSummaryRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Failed to read game scores " + id + ": " + e.getMessage(), e);
    }
    return null;
  }

  public ScoreListRepresentation getScoreHistory(int gameId) {
    return getRestClient().get(API + "games/scorehistory/" + gameId, ScoreListRepresentation.class);
  }

  public HighscoreMetadataRepresentation scanGameScore(int id) {
    try {
      return getRestClient().get(API + "games/scanscore/" + id, HighscoreMetadataRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Failed to read game scores " + id + ": " + e.getMessage(), e);
    }
    return null;
  }

  public GameRepresentation scanGame(int gameId) {
    return getRestClient().get(API + "games/scan/" + gameId, GameRepresentation.class);
  }

  public HighscoreFiles getHighscoreFiles(int gameId) {
    return getRestClient().get(API + "games/highscorefiles/" + gameId, HighscoreFiles.class);
  }

  public GameRepresentation saveGame(GameRepresentation game) throws Exception {
    try {
      return getRestClient().post(API + "games/save", game, GameRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save game: " + e.getMessage(), e);
      throw e;
    }
  }

  public GameRepresentation findMatch(String term) throws Exception {
    try {
      Map<String, String> params = new HashMap<>();
      params.put("term", term);
      return getRestClient().post(API + "games/match", params, GameRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save game: " + e.getMessage(), e);
      throw e;
    }
  }

  //--------------- avoid multiple loading in //
  /**
   * one blocking thread by emulatorId
   */
  private Map<Integer, Object> locks = new HashMap<>();

  private Object getLock(int emulatorId) {
    Object lock = null;
    synchronized (locks) {
      lock = locks.get(emulatorId);
      if (lock == null) {
        lock = new Object();
        locks.put(emulatorId, lock);
      }
    }
    return lock;
  }

  public List<GameRepresentation> getGamesByEmulator(int emulatorId) {
    return getGamesCached(emulatorId);
  }

  private List<GameRepresentation> getGamesCached(int emulatorId) {
    if (emulatorId == -1) {
      List<GameRepresentation> games = new ArrayList<>();
      List<GameEmulatorRepresentation> gameEmulators = client.getEmulatorService().getValidatedGameEmulators();
      for (GameEmulatorRepresentation gameEmulator : gameEmulators) {
        if (ignoredEmulatorIds.contains(gameEmulator.getId())) {
          continue;
        }

        List<GameRepresentation> gamesCached = getGamesCached(gameEmulator.getId());
        games.addAll(gamesCached);
      }
      return games;
    }

    // else
    if (!allGames.containsKey(emulatorId)) {
      Object lock = getLock(emulatorId);
      synchronized (lock) {
        // If a thread is already fetching data, do not start again, just wait for it
        if (loadingFlags.get(emulatorId) == null) {
          loadingFlags.put(emulatorId, Boolean.TRUE);
          // load games in a separate thread not to block the UI
          new Thread(() -> {
            LOG.info("Start the loading of known games for emulator " + emulatorId);
            List<GameRepresentation> emulatorGames = this.getKnownGames(emulatorId);
            Object lockInThread = getLock(emulatorId);
            synchronized (lockInThread) {
              // add games in cache and notify waiting thread
              this.allGames.put(emulatorId, emulatorGames);
              this.loadingFlags.remove(emulatorId);
              lockInThread.notifyAll();
            }
          }, "LoadingThreadFor_" + emulatorId).start();
        }
        try {
          lock.wait();
        }
        catch (InterruptedException ie) {
          LOG.error("The loading of known games for emulator " + emulatorId + " has been interrupted, "
              + "games may be in an inconsistant state, consider reloading the games", ie);
        }
      }
    }

    List<GameRepresentation> gameRepresentations = Collections.emptyList();
    if (allGames.containsKey(emulatorId) && allGames.get(emulatorId) != null) {
      gameRepresentations = this.allGames.get(emulatorId);
    }
    else {
      LOG.warn("Failed to load client cached games, there is no emulator with id {} available!", emulatorId);
    }

    return gameRepresentations;
  }

  public List<GameRepresentation> getVpxGamesCached() {
    List<GameRepresentation> games = new ArrayList<>();
    List<GameEmulatorRepresentation> gameEmulators = client.getEmulatorService().getValidatedGameEmulators();
    for (GameEmulatorRepresentation gameEmulator : gameEmulators) {
      if (gameEmulator.isVpxEmulator()) {
        int emulatorId = gameEmulator.getId();
        List<GameRepresentation> gamesCached = getGamesCached(emulatorId);
        games.addAll(gamesCached);
      }
    }
    return games;
  }

  public GameRepresentation getVpxGameCached(int gameId) {
    List<GameRepresentation> games = this.getVpxGamesCached();
    Optional<GameRepresentation> first = games.stream().filter(g -> g.getId() == gameId).findFirst();
    return first.orElse(null);
  }

  public ScoreSummaryRepresentation getRecentScores(int count) {
    return getRestClient().get(API + "games/recent/" + count, ScoreSummaryRepresentation.class);
  }

  public ScoreSummaryRepresentation getRecentScoresByGame(int count, int gameId) {
    return getRestClient().get(API + "games/recent/" + count + "/" + gameId, ScoreSummaryRepresentation.class);
  }

  public boolean resetHighscore(int gameId, long value) {
    Map<String, Long> values = new HashMap<>();
    values.put("gameId", (long) gameId);
    values.put("scoreValue", value);
    return getRestClient().post(API + "games/reset", values, Boolean.class);
  }

  public GameScoreValidation getGameScoreValidation(int gameId) {
    return getRestClient().get(API + "games/scorevalidation/" + gameId, GameScoreValidation.class);
  }
}
