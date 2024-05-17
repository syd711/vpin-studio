package de.mephisto.vpin.restclient.altcolor;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/*********************************************************************************************************************
 * Alt Color
 ********************************************************************************************************************/
public class AltColorServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(AltColorServiceClient.class);

  public AltColorServiceClient(VPinStudioClient client) {
    super(client);
  }

  public AltColor getAltColor(int gameId) {
    return getRestClient().get(API + "altcolor/" + gameId, AltColor.class);
  }

  public boolean delete(int gameId) {
    return getRestClient().delete(API + "altcolor/" + gameId);
  }

  public UploadDescriptor uploadAltColor(File file, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "altcolor/upload";
      ResponseEntity<UploadDescriptor> exchange = new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, gameId, null, AssetType.ALT_SOUND, listener), UploadDescriptor.class);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("ALT color upload failed: " + e.getMessage(), e);
      throw e;
    }
  }
}
