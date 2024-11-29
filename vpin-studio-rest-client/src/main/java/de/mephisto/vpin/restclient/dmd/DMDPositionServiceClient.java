package de.mephisto.vpin.restclient.dmd;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DMDPositionServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(DMDPositionServiceClient.class);

  public DMDPositionServiceClient(VPinStudioClient client) {
    super(client);
  }

  public DMDInfo getDMDInfo(int gameId) {
    return getRestClient().get(API + "dmdposition/" + gameId, DMDInfo.class);
  }

  public DMDInfo moveDMDInfo(DMDInfo dmdInfo, VPinScreen target) {
    return getRestClient().post(API + "dmdposition/move?target=" + target, dmdInfo, DMDInfo.class);
  }

  public DMDInfo autoPositionDMDInfo(DMDInfo dmdInfo) {
    return getRestClient().post(API + "dmdposition/autoPosition", dmdInfo, DMDInfo.class);
  }

  public boolean saveDMDInfo(DMDInfo dmdInfo) {
    return getRestClient().post(API + "dmdposition/save", dmdInfo, Boolean.class);
  }
}
