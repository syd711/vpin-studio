package de.mephisto.vpin.restclient.dmd;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

import java.io.ByteArrayInputStream;

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

  public DMDInfo switchDMDInfo(DMDInfo dmdInfo, DMDType type) {
    return getRestClient().post(API + "dmdposition/switch?type=" + type.name(), dmdInfo, DMDInfo.class);
  }

  public DMDInfo resetToScores(DMDInfo dmdInfo) {
    return getRestClient().post(API + "dmdposition/resetToScores", dmdInfo, DMDInfo.class);
  }

  public DMDInfo useFrontendFullDMDMedia(DMDInfo dmdInfo) {
    return getRestClient().post(API + "dmdposition/useFrontendFullDMDMedia", dmdInfo, DMDInfo.class);
  }


  public boolean saveDMDInfo(DMDInfo dmdInfo) {
    return getRestClient().post(API + "dmdposition/save", dmdInfo, Boolean.class);
  }

  public DMDInfoZone moveDMDInfo(int gameId, DMDInfoZone dmdInfoZone, VPinScreen target) {
    return getRestClient().post(API + "dmdposition/" + gameId +  "/move?target=" + target.name(), dmdInfoZone, DMDInfoZone.class);
  }

  public DMDInfoZone autoPositionDMDInfo(int gameId, DMDInfoZone dmdInfoZone) {
    return getRestClient().post(API + "dmdposition/" + gameId + "/autoPosition", dmdInfoZone, DMDInfoZone.class);
  }

  public ByteArrayInputStream getPicture(int gameId, VPinScreen onScreen) {
    byte[] bytes = getRestClient().readBinary(API + "dmdposition/picture/" + gameId + "/" + onScreen.name() + ".png");
    return (bytes != null) ? new ByteArrayInputStream(bytes) : null;
  }
}
