package de.mephisto.vpin.restclient.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

/*********************************************************************************************************************
 * VPS Service
 ********************************************************************************************************************/
public class VpsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VpsServiceClient.class);

  VpsServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean autofill(int gameId, boolean overwrite) {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "vps/autofill/" + gameId + "/" + overwrite, Boolean.class);
  }

  public void saveTable(int gameId, String vpsId) {
    try {
      getRestClient().put(API + "vps/table/" + gameId + "/" + vpsId, new HashMap<>());
    } catch (Exception e) {
      LOG.error("Failed to save mapping: " + e.getMessage(), e);
    }
  }

  public void saveVersion(int gameId, String vpsId) {
    try {
      getRestClient().put(API + "vps/version/" + gameId + "/" + vpsId, new HashMap<>());
    } catch (Exception e) {
      LOG.error("Failed to save mapping: " + e.getMessage(), e);
    }
  }
}
