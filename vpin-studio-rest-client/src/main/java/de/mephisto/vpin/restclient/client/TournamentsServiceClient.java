package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.connectors.mania.ManiaServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************************************
 * Tournamens
 ********************************************************************************************************************/
public class TournamentsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(TournamentsServiceClient.class);

  TournamentsServiceClient(VPinStudioClient client) {
    super(client);
  }

  public ManiaServiceConfig getConfig() {
    return getRestClient().get(API + "tournaments/config", ManiaServiceConfig.class);
  }
}
