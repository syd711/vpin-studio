package de.mephisto.vpin.restclient.dmd;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DMDPositionServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionServiceClient.class);

  public DMDPositionServiceClient(VPinStudioClient client) {
    super(client);
  }

  public DMDInfo getDMDInfo(int gameId) {
    return getRestClient().get(API + "dmdposition/" + gameId, DMDInfo.class);
  }

}
