package de.mephisto.vpin.restclient.games;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
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

  public RomServiceClient(VPinStudioClient client) {
    super(client);
  }

  public void deleteAliasMapping(int emuId, String alias) {
    getRestClient().delete(API + "rom/mapping/" + emuId + "/" + alias);
  }
}
