package de.mephisto.vpin.restclient.games;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.restclient.highscores.HighscoreMetadataRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreListRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
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

  private Map<Integer, List<GameRepresentation>> allGames = new HashMap<>();
  /**
   * a status map to avoid multiple loads in parallel, check getGamesCached()
   */
  private Map<Integer, Boolean> loadingFlags = new HashMap<>();

  public GamesServiceClient(VPinStudioClient client) {
    super(client);
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

  public UploadDescriptor uploadTable(File file, TableUploadType tableUploadDescriptor, int gameId, int emuId, FileUploadProgressListener listener) {
    try {
      String url = getRestClient().getBaseUrl() + API + "games/upload/table";
      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add("mode", tableUploadDescriptor.name());
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
      return getRestClient().post(API + "games/process/table", uploadDescriptor, UploadDescriptor.class);
    }
    catch (Exception e) {
      LOG.error("Failed to process table: " + e.getMessage(), e);
      throw e;
    }
  }

  public void deleteGame(@NonNull DeleteDescriptor descriptor) {
    try {
      getRestClient().post(API + "games/delete", descriptor, Boolean.class);
    }
    catch (Exception e) {
      LOG.error("Failed to delete games " + descriptor.getGameIds() + ": " + e.getMessage(), e);
    }
  }

  public List<Integer> filterGames(@NonNull FilterSettings filterSettings) {
    try {
      return new ArrayList<>(Arrays.asList(getRestClient().post(API + "games/filter", filterSettings, Integer[].class)));
    }
    catch (Exception e) {
      LOG.error("Failed to filter games: " + e.getMessage(), e);
    }
    return null;
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

        List<GameRepresentation> all = this.getGamesCached(-1);
        index = all.indexOf(gameRepresentation);
        if (index != -1) {
          all.remove(index);
          all.add(index, gameRepresentation);
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

  public List<GameRepresentation> getGamesCached(int emulatorId) {
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
    return this.allGames.get(emulatorId);
  }

  public List<GameRepresentation> getVpxGamesCached() {
    return getGamesCached(-1).stream().filter(g -> g.isVpxGame()).collect(Collectors.toList());
  }

  public GameRepresentation getGameCached(int gameId) {
    List<GameRepresentation> games = this.getGamesCached(-1);
    Optional<GameRepresentation> first = games.stream().filter(g -> g.getId() == gameId).findFirst();
    return first.orElse(null);
  }

  public ScoreSummaryRepresentation getRecentScores(int count) {
    return getRestClient().get(API + "games/recent/" + count, ScoreSummaryRepresentation.class);
  }

  public ScoreSummaryRepresentation getRecentScoresByGame(int count, int gameId) {
    return getRestClient().get(API + "games/recent/" + count + "/" + gameId, ScoreSummaryRepresentation.class);
  }

  public boolean resetHighscore(int gameId) {
    return getRestClient().delete(API + "games/reset/" + gameId);
  }

  public GameScoreValidation getGameScoreValidation(int gameId) {
    return getRestClient().get(API + "games/scorevalidation/" + gameId, GameScoreValidation.class);
  }
}
