package de.mephisto.vpin.restclient.competitions;

import de.mephisto.vpin.connectors.iscored.IScoredGame;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.highscores.ScoreListRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/*********************************************************************************************************************
 * Competitions
 ********************************************************************************************************************/
public class CompetitionsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public CompetitionsServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean hasManagePermissions(long serverId, long channelId) {
    return getRestClient().get(API + "discord/permissions/competitions/manage/" + serverId + "/" + channelId, Boolean.class);
  }

  public boolean hasManagePermissions(long serverId) {
    return getRestClient().get(API + "discord/permissions/competitions/manage/" + serverId, Boolean.class);
  }

  public boolean hasJoinPermissions(long serverId, long channelId) {
    return getRestClient().get(API + "discord/permissions/competitions/join/" + serverId + "/" + channelId, Boolean.class);
  }

  public CompetitionRepresentation getCompetitionByUuid(String uuid) {
    return getRestClient().get(API + "competitions/competition/" + uuid, CompetitionRepresentation.class);
  }

  public List<CompetitionRepresentation> getOfflineCompetitions() {
    return Arrays.asList(getRestClient().get(API + "competitions/offline", CompetitionRepresentation[].class));
  }

  public List<PlayerRepresentation> getDiscordCompetitionPlayers(long competitionId) {
    return Arrays.asList(getRestClient().get(API + "competitions/players/" + competitionId, PlayerRepresentation[].class));
  }

  public List<CompetitionRepresentation> getDiscordCompetitions() {
    return Arrays.asList(getRestClient().get(API + "competitions/discord", CompetitionRepresentation[].class));
  }


  public List<CompetitionRepresentation> getSubscriptions() {
    return Arrays.asList(getRestClient().get(API + "competitions/subscriptions", CompetitionRepresentation[].class));
  }


  public List<CompetitionRepresentation> getIScoredSubscriptions() {
    return Arrays.asList(getRestClient().get(API + "competitions/iscored", CompetitionRepresentation[].class));
  }

  public List<CompetitionRepresentation> getFinishedCompetitions(int limit) {
    return Arrays.asList(getRestClient().get(API + "competitions/finished/" + limit, CompetitionRepresentation[].class));
  }


  public CompetitionRepresentation getActiveCompetition(CompetitionType type) {
    try {
      return getRestClient().get(API + "competitions/" + type.name() + "/active", CompetitionRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Failed to read active competition: " + e.getMessage(), e);
    }
    return null;
  }

  public CompetitionRepresentation saveCompetition(CompetitionRepresentation c) throws Exception {
    try {
      return getRestClient().post(API + "competitions/save", c, CompetitionRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save competition: " + e.getMessage(), e);
      throw e;
    }
  }

  public List<CompetitionRepresentation> getGameCompetitions(int gameId) {
    return Arrays.asList(getRestClient().get(API + "competitions/game/" + gameId, CompetitionRepresentation[].class));
  }

  public void deleteCompetition(CompetitionRepresentation c) {
    try {
      getRestClient().delete(API + "competitions/" + c.getId());
      String cacheId = "competition-bg-game-" + c.getGameId();
      client.getImageCache().clear(cacheId);
    }
    catch (Exception e) {
      LOG.error("Failed to delete competition: " + e.getMessage(), e);
    }
  }

  public void finishCompetition(CompetitionRepresentation c) {
    try {
      getRestClient().put(API + "competitions/finish/" + c.getId(), Collections.emptyMap());
    }
    catch (Exception e) {
      LOG.error("Failed to finish competition: " + e.getMessage(), e);
    }
  }

  public ScoreListRepresentation getCompetitionScoreList(long competitionId) {
    try {
      return getRestClient().get(API + "competitions/scores/" + competitionId, ScoreListRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Failed to read competition scores list " + competitionId + ": " + e.getMessage(), e);
    }
    return null;
  }

  public ScoreSummaryRepresentation getCompetitionScore(long id) {
    try {
      return getRestClient().get(API + "competitions/score/" + id, ScoreSummaryRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Failed to read competition scores " + id + ": " + e.getMessage(), e);
    }
    return null;
  }

  public boolean synchronizeGameRooms() {
    try {
      return getRestClient().post(API + "competitions/iscored/synchronize", new HashMap<>(), Boolean.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save competition: " + e.getMessage(), e);
      throw e;
    }
  }

  public IScoredSyncModel synchronizeIScoredGameRoomGame(@NonNull IScoredGameRoom gameRoom, @NonNull IScoredGame next, boolean invalidate, boolean manualSubscription) {
    try {
      IScoredSyncModel sync = new IScoredSyncModel();
      sync.setGame(next);
      sync.setiScoredGameRoom(gameRoom);
      sync.setManualSubscription(manualSubscription);
      sync.setInvalidate(invalidate);
      return getRestClient().post(API + "competitions/iscored/synchronizeGameRoom", sync, IScoredSyncModel.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save competition: " + e.getMessage(), e);
      throw e;
    }
  }

  public ByteArrayInputStream getCompetitionBackground(long gameId) {
    String name = "competition-bg-game-" + gameId;

    if (!client.getImageCache().containsKey(name)) {
      byte[] bytes = getRestClient().readBinary(API + "assets/competition/" + gameId);
      if (bytes != null) {
        client.getImageCache().put(name, bytes);
      }
    }

    byte[] imageBytes = client.getImageCache().get(name);
    return new ByteArrayInputStream(imageBytes);
  }


  public List<String> getCompetitionBadges() {
    return Arrays.asList(getRestClient().get(API + "system/badges", String[].class));
  }

  public ByteArrayInputStream getCompetitionBadge(String name) {
    if (!client.getImageCache().containsKey(name)) {
      String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
      byte[] bytes = getRestClient().readBinary(API + "system/badge/" + encodedName);
      client.getImageCache().put(name, bytes);
    }

    byte[] imageBytes = client.getImageCache().get(name);
    return new ByteArrayInputStream(imageBytes);
  }
}
