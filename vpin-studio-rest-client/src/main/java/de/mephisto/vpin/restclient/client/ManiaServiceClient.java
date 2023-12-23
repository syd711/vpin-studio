package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.connectors.mania.ManiaServiceConfig;
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

  public ManiaServiceConfig getConfig() {
    return getRestClient().get(API + "mania/config", ManiaServiceConfig.class);
  }
}
