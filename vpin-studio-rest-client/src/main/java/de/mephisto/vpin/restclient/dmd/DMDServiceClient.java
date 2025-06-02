package de.mephisto.vpin.restclient.dmd;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;


public class DMDServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(DMDServiceClient.class);

  public DMDServiceClient(VPinStudioClient client) {
    super(client);
  }

  public DMDPackage getDMDPackage(int gameId) {
    return getRestClient().get(API + "dmd/" + gameId, DMDPackage.class);
  }

  public UploadDescriptor uploadDMDPackage(File file, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "dmd/upload";
      HttpEntity upload = createUpload(file, gameId, null, AssetType.DMD_PACK, listener);
      ResponseEntity<UploadDescriptor> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, upload, UploadDescriptor.class);
      finalizeUpload(upload);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("DMD upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public ComponentSummary getFreezySummary() {
    return getRestClient().get(API + "dmd/freezy", ComponentSummary.class);
  }

  public boolean clearCache() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "dmd/clearcache", Boolean.class);
  }
}
