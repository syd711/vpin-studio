package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.AssetType;
import de.mephisto.vpin.restclient.FileUploadProgressListener;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.jobs.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.representations.PupPackRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.File;


public class PupPackServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(PupPackServiceClient.class);

  PupPackServiceClient(VPinStudioClient client) {
    super(client);
  }

  public PupPackRepresentation getPupPack(int gameId) {
    return getRestClient().get(API + "puppacks/" + gameId, PupPackRepresentation.class);
  }

  public boolean clearCache() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "puppacks/clearcache", Boolean.class);
  }

  public boolean setPupPackEnabled(int gameId, boolean b) {
    return getRestClient().get(API + "puppacks/set/" + gameId + "/" + b, Boolean.class);
  }

  public boolean isPupPackEnabled(int gameId) {
    return getRestClient().get(API + "puppacks/enabled/" + gameId, Boolean.class);
  }

  public JobExecutionResult option(int gameId, String option) throws Exception {
    CommandOption o = new CommandOption();
    o.setCommand(option);
    return getRestClient().post(API + "puppacks/option/" + gameId, o, JobExecutionResult.class);
  }

  public JobExecutionResult uploadPupPack(File file, String uploadType, int gameId, FileUploadProgressListener listener) {
    try {
      String url = getRestClient().getBaseUrl() + API + "puppacks/upload";
      ResponseEntity<JobExecutionResult> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, createUpload(file, gameId, uploadType, AssetType.PUP_PACK, listener), JobExecutionResult.class);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("PUP pack upload failed: " + e.getMessage(), e);
      return JobExecutionResultFactory.error("ALT sound upload failed: " + e.getMessage());
    }
  }
}
