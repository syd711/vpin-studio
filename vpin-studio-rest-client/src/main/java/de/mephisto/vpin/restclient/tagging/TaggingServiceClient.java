package de.mephisto.vpin.restclient.tagging;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * Tagging
 ********************************************************************************************************************/
public class TaggingServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public TaggingServiceClient(VPinStudioClient client) {
    super(client);
  }

  public List<String> getTags() {
    return new ArrayList<>(Arrays.asList(getRestClient().get(API + "tagging", String[].class)));
  }
}
