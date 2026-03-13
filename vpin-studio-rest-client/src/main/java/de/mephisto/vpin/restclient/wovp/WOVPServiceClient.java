package de.mephisto.vpin.restclient.wovp;

import de.mephisto.vpin.connectors.wovp.models.ApiKeyValidationResponse;
import de.mephisto.vpin.connectors.wovp.models.WovpPlayer;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * Wovp
 ********************************************************************************************************************/
public class WOVPServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(WOVPServiceClient.class);

  public WOVPServiceClient(VPinStudioClient client) {
    super(client);
  }

  public ApiKeyValidationResponse test(@NonNull String key) {
    return getRestClient().get(API + "wovp/test/" + key, ApiKeyValidationResponse.class);
  }

  public List<WovpPlayer> getPlayers() {
    return Arrays.asList(getRestClient().get(API + "wovp/players", WovpPlayer[].class));
  }

  public boolean clearCache() {
    return getRestClient().get(API + "wovp/clearCache", Boolean.class);
  }
}
