package de.mephisto.vpin.restclient.players;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.players.PlayerDomain;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.players.RankedPlayerRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


/*********************************************************************************************************************
 * Player
 ********************************************************************************************************************/
public class PlayersServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public PlayersServiceClient(VPinStudioClient client) {
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
    return getRestClient().get(API + "players/highscores/" + URLEncoder.encode(initials, StandardCharsets.UTF_8), ScoreSummaryRepresentation.class);
  }

  public PlayerRepresentation getDefaultPlayer() {
    List<PlayerRepresentation> players = this.getPlayers();
    for (PlayerRepresentation player : players) {
      if (player.isAdministrative()) {
        return player;
      }
    }
    return null;
  }
}
