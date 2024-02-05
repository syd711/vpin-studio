package de.mephisto.vpin.restclient.games;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.highscores.HighscoreMetadataRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreListRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import de.mephisto.vpin.restclient.validation.ValidationState;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/*********************************************************************************************************************
 * Games
 ********************************************************************************************************************/
public class GamesServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  private List<GameRepresentation> games = new ArrayList<>();

  public GamesServiceClient(VPinStudioClient client) {
    super(client);
  }


  public void clearCache() {
    this.games = new ArrayList<>();
  }

  public boolean uploadRom(int emuId, File file, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "games/upload/rom/" + emuId;
      return Boolean.TRUE.equals(createUploadTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, AssetType.ROM, listener), Boolean.class).getBody());
    } catch (Exception e) {
      LOG.error("Rom upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public void deleteGame(@NonNull DeleteDescriptor descriptor) {
    try {
      getRestClient().post(API + "games/delete", descriptor, Boolean.class);
    } catch (Exception e) {
      LOG.error("Failed to delete games " + descriptor.getGameIds() + ": " + e.getMessage(), e);
    }
  }

  public List<GameRepresentation> getGamesByRom(String rom) {
    List<GameRepresentation> gameList = this.getGamesCached();
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
    List<GameRepresentation> gameList = this.getGamesCached();
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
      if (gameRepresentation != null && !this.games.isEmpty()) {
        int index = this.games.indexOf(gameRepresentation);
        if (index != -1) {
          this.games.remove(index);
          this.games.add(index, gameRepresentation);
        }
      }
      return gameRepresentation;
    } catch (Exception e) {
      LOG.error("Failed to retrieve game: " + e.getMessage());
    }
    return null;
  }

  public List<Integer> getUnknownGameIds() {
    try {
      List<Integer> unknowns = Arrays.asList(getRestClient().get(API + "games/unknowns", Integer[].class));
      if (!unknowns.isEmpty()) {
        this.games.clear();
      }
      return unknowns;
    } catch (Exception e) {
      LOG.error("Failed to read unknowns game ids: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  public List<GameRepresentation> getKnownGames() {
    try {
      this.games = new ArrayList<>(Arrays.asList(getRestClient().get(API + "games/knowns", GameRepresentation[].class)));
      return this.games;
    } catch (Exception e) {
      LOG.error("Failed to read knowns games: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  @Deprecated
  public List<GameRepresentation> getGames() {
    try {
      this.games = new ArrayList<>(Arrays.asList(getRestClient().get(API + "games", GameRepresentation[].class)));
      return this.games;
    } catch (Exception e) {
      LOG.error("Failed to get games: " + e.getMessage(), e);
      throw e;
    }
  }

  @Nullable
  public GameRepresentation getGameByVpsTable(@NonNull VpsTable vpsTable, @Nullable VpsTableVersion vpsTableVersion) {
    List<GameRepresentation> gamesCached = getGamesCached();
    GameRepresentation hit = null;
    for (GameRepresentation game : gamesCached) {
      if (!StringUtils.isEmpty(game.getExtTableId()) && game.getExtTableId().equals(vpsTable.getId())) {
        if (vpsTableVersion == null) {
          hit = game;
          break;
        }

        if (!StringUtils.isEmpty(game.getExtTableVersionId()) && game.getExtTableVersionId().equals(vpsTableVersion.getId())) {
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
    } catch (Exception e) {
      LOG.error("Failed to read game ids: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  public ScoreSummaryRepresentation getGameScores(int id) {
    try {
      return getRestClient().get(API + "games/scores/" + id, ScoreSummaryRepresentation.class);
    } catch (Exception e) {
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
    } catch (Exception e) {
      LOG.error("Failed to read game scores " + id + ": " + e.getMessage(), e);
    }
    return null;
  }

  public GameRepresentation scanGame(int gameId) {
    return getRestClient().get(API + "games/scan/" + gameId, GameRepresentation.class);
  }

  public GameRepresentation saveGame(GameRepresentation game) throws Exception {
    try {
      return getRestClient().post(API + "games/save", game, GameRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save game: " + e.getMessage(), e);
      throw e;
    }
  }

  public List<GameRepresentation> getGamesWithScores() {
    List<GameRepresentation> gameList = this.getGamesCached();
    List<GameRepresentation> result = new ArrayList<>();
    for (GameRepresentation gameRepresentation : gameList) {
      if (!StringUtils.isEmpty(gameRepresentation.getHighscoreType())) {
        result.add(gameRepresentation);
      }
    }
    return result;
  }

  public List<GameRepresentation> getGamesCached() {
    if (this.games == null || this.games.isEmpty()) {
      this.games = this.getKnownGames();
    }
    return this.games;
  }

  public GameRepresentation getGameCached(int gameId) {
    if (this.games == null || this.games.isEmpty()) {
      this.games = this.getKnownGames();
    }
    return this.games.stream().filter(g -> g.getId() == gameId).findFirst().orElseGet(null);
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
}
