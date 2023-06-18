package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.mame.MameOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************************************
 * Mame
 ********************************************************************************************************************/
public class MameServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MameServiceClient.class);

  MameServiceClient(VPinStudioClient client) {
    super(client);
  }

  public MameOptions getOptions(String name) {
    return getRestClient().get(API + "mame/options/" + name, MameOptions.class);
  }

  public MameOptions saveOptions(MameOptions options) throws Exception {
    return getRestClient().post(API + "mame/options/", options, MameOptions.class);
  }
}
