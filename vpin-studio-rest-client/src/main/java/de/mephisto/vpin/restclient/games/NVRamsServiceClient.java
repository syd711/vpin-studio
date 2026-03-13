package de.mephisto.vpin.restclient.games;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.highscores.NVRamList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/*********************************************************************************************************************
 * NV Rams
 ********************************************************************************************************************/
public class NVRamsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private NVRamList nvRamList;

  public NVRamsServiceClient(VPinStudioClient client) {
    super(client);
  }


  public NVRamList getResettedNVRams() {
    if (nvRamList == null) {
      nvRamList = getRestClient().get(API + "nvrams", NVRamList.class);
    }
    return nvRamList;
  }

  public void clearCache() {
    this.nvRamList = null;
  }
}
