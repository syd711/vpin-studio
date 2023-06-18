package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.*;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
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

  AltColorServiceClient(VPinStudioClient client) {
    super(client);
  }

  public AltColor getAltColor(int gameId) {
    return getRestClient().get(API + "altcolor/" + gameId, AltColor.class);
  }

  public JobExecutionResult uploadAltColor(File file, String uploadType, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "altcolor/upload";
      ResponseEntity<JobExecutionResult> exchange = new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, gameId, uploadType, AssetType.ALT_SOUND, listener), JobExecutionResult.class);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("ALT color upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean clearCache() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "altsound/clearcache", Boolean.class);
  }
}
