package de.mephisto.vpin.restclient.vps;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.popper.TableDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

/*********************************************************************************************************************
 * VPS Service
 ********************************************************************************************************************/
public class VpsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VpsServiceClient.class);

  public VpsServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean autoMatch(int gameId, boolean overwrite) {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "vps/automatch/" + gameId + "/" + overwrite, Boolean.class);
  }
}
