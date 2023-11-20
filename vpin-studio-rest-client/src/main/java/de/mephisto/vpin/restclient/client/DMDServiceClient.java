package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.dmd.DMDPackage;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;


public class DMDServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(DMDServiceClient.class);

  DMDServiceClient(VPinStudioClient client) {
    super(client);
  }

  public DMDPackage getDMDPackage(int gameId) {
    return getRestClient().get(API + "dmd/" + gameId, DMDPackage.class);
  }

  public JobExecutionResult uploadDMDPackage(File file, String uploadType, int gameId, FileUploadProgressListener listener) {
    try {
      String url = getRestClient().getBaseUrl() + API + "dmd/upload";
      ResponseEntity<JobExecutionResult> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, createUpload(file, gameId, uploadType, AssetType.DMD_PACK, listener), JobExecutionResult.class);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("DMD upload failed: " + e.getMessage(), e);
      return JobExecutionResultFactory.error("DMD upload failed: " + e.getMessage());
    }
  }

  public ComponentSummary getFreezySummary(int emulatorId) {
    return getRestClient().get(API + "dmd/freezy/" + emulatorId, ComponentSummary.class);
  }

  public boolean clearCache() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "dmd/clearcache", Boolean.class);
  }
}
