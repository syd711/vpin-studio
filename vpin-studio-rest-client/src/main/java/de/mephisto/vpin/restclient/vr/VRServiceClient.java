package de.mephisto.vpin.restclient.vr;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.File;
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

  public GameEmulatorScript getVrLaunchScript(int emulatorId) {
    return getRestClient().get(API + "vr/launchscript/" + emulatorId, GameEmulatorScript.class);
  }

  public VRFilesInfo getVRFiles(int emulatorId) {
    return getRestClient().get(API + "vr/files/" + emulatorId, VRFilesInfo.class);
  }

  public GameEmulatorScript saveVrEmulatorLaunchScript(int emulatorId, GameEmulatorScript script) {
    try {
      return getRestClient().post(API + "vr/save/" + emulatorId, script, GameEmulatorScript.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save GameEmulatorScript: {}", e.getMessage(), e);
      throw e;
    }
  }

  public boolean uploadFile(File file, int emulatorId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "vr/files/" + emulatorId;
      HttpEntity upload = createUpload(file, emulatorId, null, AssetType.INI, listener);
      ResponseEntity<Boolean> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, upload, Boolean.class);
      finalizeUpload(upload);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("VR file upload failed: {}", e.getMessage(), e);
      throw e;
    }
  }
}
