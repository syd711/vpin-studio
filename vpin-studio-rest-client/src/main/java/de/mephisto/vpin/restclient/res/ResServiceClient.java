package de.mephisto.vpin.restclient.res;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*********************************************************************************************************************
 * RES
 ********************************************************************************************************************/
public class ResServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public ResServiceClient(VPinStudioClient client) {
    super(client);
  }

//  public boolean deleteRes(int gameId) throws Exception {
//    Map<String, Object> params = new HashMap<>();
//    params.put("emulatorId", directB2S.getEmulatorId());
//    params.put("fileName", directB2S.getFileName());
//    return getRestClient().post(API + "directb2s/delete", directB2S, Boolean.class);
//  }

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
