package de.mephisto.vpin.restclient.fp;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

import java.io.File;
import java.lang.invoke.MethodHandles;

/*********************************************************************************************************************
 * FP
 ********************************************************************************************************************/
public class FpServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public FpServiceClient(VPinStudioClient client) {
    super(client);
  }

  public UploadDescriptor uploadCfg(int gameId, File file, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "fp/upload/cfg/" + gameId;
      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      ResponseEntity<UploadDescriptor> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, createUpload(map, file, gameId, null, AssetType.CFG, listener), UploadDescriptor.class);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("Rom upload failed: " + e.getMessage(), e);
      throw e;
    }
  }
}
