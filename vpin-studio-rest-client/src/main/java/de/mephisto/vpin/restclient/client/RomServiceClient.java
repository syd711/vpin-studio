package de.mephisto.vpin.restclient.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/*********************************************************************************************************************
 * ROM Service
 ********************************************************************************************************************/
public class RomServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(RomServiceClient.class);

  RomServiceClient(VPinStudioClient client) {
    super(client);
  }

  public void saveAliasMapping(String existingAlias, String key, String value) {
    Map<String, Object> values = new HashMap<>();
    values.put(key, value);
    values.put("#oldValue", existingAlias);
    try {
      getRestClient().put(API + "rom/mapping", values);
    } catch (Exception e) {
      LOG.error("Failed to save mapping: " + e.getMessage(), e);
    }
  }

  public boolean clearCache() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "rom/clearcache", Boolean.class);
  }
}
