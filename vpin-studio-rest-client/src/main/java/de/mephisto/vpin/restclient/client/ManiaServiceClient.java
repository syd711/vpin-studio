package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.mania.ManiaAccountRepresentation;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************************************
 * VPin Mania
 ********************************************************************************************************************/
public class ManiaServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(ManiaServiceClient.class);

  ManiaServiceClient(VPinStudioClient client) {
    super(client);
  }

  public ManiaAccountRepresentation getAccount() {
    return getRestClient().get(API + "mania/account", ManiaAccountRepresentation.class);
  }

  public ManiaAccountRepresentation saveAccount(ManiaAccountRepresentation account) throws Exception {
    try {
      return getRestClient().post(API + "mania/account/save", account, ManiaAccountRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save account: " + e.getMessage(), e);
      throw e;
    }
  }
}
