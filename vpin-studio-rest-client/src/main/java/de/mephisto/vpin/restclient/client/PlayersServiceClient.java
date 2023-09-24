package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.players.PlayerDomain;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.players.RankedPlayerRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


/*********************************************************************************************************************
 * Player
 ********************************************************************************************************************/
public class PlayersServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  PlayersServiceClient(VPinStudioClient client) {
    super(client);
  }

  public PlayerRepresentation savePlayer(PlayerRepresentation p) throws Exception {
    try {
      return getRestClient().post(API + "players/save", p, PlayerRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save player: " + e.getMessage(), e);
      throw e;
    }
  }

  public void deletePlayer(PlayerRepresentation p) {
    try {
      getRestClient().delete(API + "players/" + p.getId());
    } catch (Exception e) {
      LOG.error("Failed to delete player: " + e.getMessage(), e);
    }
  }

  public List<PlayerRepresentation> getPlayers() {
    return Arrays.asList(getRestClient().get(API + "players", PlayerRepresentation[].class));
  }

  public List<PlayerRepresentation> getPlayers(PlayerDomain domain) {
    return Arrays.asList(getRestClient().get(API + "players/domain/" + domain.name(), PlayerRepresentation[].class));
  }

  public PlayerRepresentation getPlayer(long serverId, String initials) {
    try {
      return getRestClient().get(API + "players/player/" + serverId + "/" + initials, PlayerRepresentation.class);
    } catch (Exception e) {
      //ignore
    }
    return null;
  }

  public List<RankedPlayerRepresentation> getRankedPlayers() {
    return Arrays.asList(getRestClient().get(API + "players/ranked", RankedPlayerRepresentation[].class));
  }

  public ScoreSummaryRepresentation getPlayerScores(String initials) {
    return getRestClient().get(API + "players/highscores/" + initials, ScoreSummaryRepresentation.class);
  }
}
