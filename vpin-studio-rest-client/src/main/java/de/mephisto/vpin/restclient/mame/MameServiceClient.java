package de.mephisto.vpin.restclient.mame;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;

/*********************************************************************************************************************
 * Mame
 ********************************************************************************************************************/
public class MameServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MameServiceClient.class);

  public MameServiceClient(VPinStudioClient client) {
    super(client);
  }

  public MameOptions getOptions(String name) {
    return getRestClient().get(API + "mame/options/" + name, MameOptions.class);
  }

  public boolean clearCache() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "mame/clearcache", Boolean.class);
  }

  public MameOptions saveOptions(MameOptions options) throws Exception {
    return getRestClient().post(API + "mame/options/", options, MameOptions.class);
  }
}