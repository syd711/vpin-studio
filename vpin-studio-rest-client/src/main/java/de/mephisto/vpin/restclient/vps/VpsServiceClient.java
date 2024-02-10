package de.mephisto.vpin.restclient.vps;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.popper.TableDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************************************
 * VPS Service
 ********************************************************************************************************************/
public class VpsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VpsServiceClient.class);

  public VpsServiceClient(VPinStudioClient client) {
    super(client);
  }

  public TableDetails autoMatch(int gameId, boolean overwrite) {
    return getRestClient().get(API + "vps/automatch/" + gameId + "/" + overwrite, TableDetails.class);
  }
}
