package de.mephisto.vpin.restclient.mania;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************************************
 * Mania
 ********************************************************************************************************************/
public class ManiaServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaServiceClient.class);

  public ManiaServiceClient(VPinStudioClient client) {
    super(client);
  }

  public ManiaConfig getConfig() {
    return getRestClient().get(API + "mania/config", ManiaConfig.class);
  }

  public boolean synchronizeHighscore(String vpsTableId) {
    return getRestClient().get(API + "mania/scoresync/" + vpsTableId, Boolean.class);
  }

}
