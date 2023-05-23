package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.AssetType;
import de.mephisto.vpin.restclient.FileUploadProgressListener;
import de.mephisto.vpin.restclient.descriptors.DeleteDescriptor;
import de.mephisto.vpin.restclient.descriptors.ResetHighscoreDescriptor;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.HighscoreMetadataRepresentation;
import de.mephisto.vpin.restclient.representations.ScoreListRepresentation;
import de.mephisto.vpin.restclient.representations.ScoreSummaryRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/*********************************************************************************************************************
 * Games
 ********************************************************************************************************************/
public class GamesServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  GamesServiceClient(VPinStudioClient client) {
    super(client);
  }


  public boolean uploadRom(File file, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "games/upload/rom";
      return Boolean.TRUE.equals(new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, AssetType.ROM, listener), Boolean.class).getBody());
    } catch (Exception e) {
      LOG.error("Rom upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public void deleteGame(@NonNull DeleteDescriptor descriptor) {
    try {
      getRestClient().post(API + "games/delete", descriptor, Boolean.class);
    } catch (Exception e) {
      LOG.error("Failed to delete game " + descriptor.getGameId() + ": " + e.getMessage(), e);
    }
  }

  public List<GameRepresentation> getGamesByRom(String rom) {
    try {
      return Arrays.asList(getRestClient().get(API + "games/rom/" + rom, GameRepresentation[].class));
    } catch (Exception e) {
      LOG.error("Failed to read games for " + rom + ": " + e.getMessage());
    }
    return null;
  }


  public GameRepresentation getGame(int id) {
    try {
      return getRestClient().getCached(API + "games/" + id, GameRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to read game " + id + ": " + e.getMessage());
    }
    return null;
  }

  public int getGameCount() {
    try {
      final RestTemplate restTemplate = new RestTemplate();
      return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "games/count", Integer.class);
    } catch (Exception e) {
      LOG.error("Failed to read game count: " + e.getMessage(), e);
    }
    return 0;
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

  public List<GameRepresentation> getGames() {
    try {
      return Arrays.asList(getRestClient().get(API + "games", GameRepresentation[].class));
    } catch (Exception e) {
      LOG.error("Failed to save game: " + e.getMessage(), e);
      throw e;
    }
  }

  public List<GameRepresentation> getGamesWithScores() {
    return Arrays.asList(getRestClient().get(API + "games/scoredgames", GameRepresentation[].class));
  }

  public ScoreSummaryRepresentation getRecentlyPlayedGames(int count) {
    return getRestClient().get(API + "games/recent/" + count, ScoreSummaryRepresentation.class);
  }

  public boolean resetHighscore(ResetHighscoreDescriptor descriptor) throws Exception {
    return getRestClient().post(API + "games/reset", descriptor, Boolean.class);
  }
}
