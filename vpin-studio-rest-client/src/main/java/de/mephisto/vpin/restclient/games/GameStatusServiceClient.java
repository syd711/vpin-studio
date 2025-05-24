package de.mephisto.vpin.restclient.games;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************************************
 * Game Status
 ********************************************************************************************************************/
public class GameStatusServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(GameStatusServiceClient.class);

  public GameStatusServiceClient(VPinStudioClient client) {
    super(client);
  }

  public GameStatus getStatus() {
    return getRestClient().get(API + "gamestatus", GameStatus.class);
  }

  public GameStatus startPause() {
    return getRestClient().get(API + "gamestatus/paused", GameStatus.class);
  }

  public GameStatus finishPause() {
    return getRestClient().get(API + "gamestatus/unpaused", GameStatus.class);
  }
}
