package de.mephisto.vpin.restclient.res;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/*********************************************************************************************************************
 * RES
 ********************************************************************************************************************/
public class ResServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public ResServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean deleteRes(int emulatorId, String filename) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("emulatorId", emulatorId);
    params.put("fileName", filename);
    return getRestClient().post(API + "res/delete", params, Boolean.class);
  }

  public void delete(int gameId) {
    getRestClient().delete(API + "res/" + gameId);
  }

  public UploadDescriptor uploadResFile(File file, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "res/upload";
      HttpEntity upload = createUpload(file, gameId, null, AssetType.RES, listener);
      ResponseEntity<UploadDescriptor> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, upload, UploadDescriptor.class);
      finalizeUpload(upload);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("Res upload failed: " + e.getMessage(), e);
      throw e;
    }
  }
}
