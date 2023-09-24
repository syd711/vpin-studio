package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.highscores.NVRamList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*********************************************************************************************************************
 * NV Rams
 ********************************************************************************************************************/
public class NVRamsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  NVRamsServiceClient(VPinStudioClient client) {
    super(client);
  }


  public NVRamList getResettedNVRams() {
    return getRestClient().get(API + "nvrams", NVRamList.class);
  }
}
