package de.mephisto.vpin.restclient.vr;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/*********************************************************************************************************************
 * VR
 ********************************************************************************************************************/
public class VRServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public VRServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean toggleVR() {
    return getRestClient().get(API + "vr/toggle", Boolean.class);
  }

//  public UploadDescriptor uploadMusic(File file, int emulatorId, int gameId, FileUploadProgressListener listener) throws Exception {
//    try {
//      String url = getRestClient().getBaseUrl() + API + "vpx/music/upload/" + emulatorId;
//      HttpEntity upload = createUpload(file, gameId, null, AssetType.MUSIC, listener);
//      ResponseEntity<UploadDescriptor> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, upload, UploadDescriptor.class);
//      finalizeUpload(upload);
//      return exchange.getBody();
//    }
//    catch (Exception e) {
//      LOG.error("Music upload failed: " + e.getMessage(), e);
//      throw e;
//    }
//  }
}
