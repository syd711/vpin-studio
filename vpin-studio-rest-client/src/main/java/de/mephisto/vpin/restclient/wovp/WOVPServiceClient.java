package de.mephisto.vpin.restclient.wovp;

import de.mephisto.vpin.connectors.wovp.models.ApiKeyValidationResponse;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************************************
 * Wovp
 ********************************************************************************************************************/
public class WOVPServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(WOVPServiceClient.class);

  public WOVPServiceClient(VPinStudioClient client) {
    super(client);
  }

  public ApiKeyValidationResponse test() {
    return getRestClient().get(API + "wovp/test", ApiKeyValidationResponse.class);
  }
}
