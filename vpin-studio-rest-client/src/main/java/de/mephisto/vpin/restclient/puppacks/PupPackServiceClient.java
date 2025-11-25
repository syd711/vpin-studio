package de.mephisto.vpin.restclient.puppacks;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.CommandOption;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.lang.invoke.MethodHandles;


public class PupPackServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public PupPackServiceClient(VPinStudioClient client) {
    super(client);
  }

  public PupPackRepresentation getPupPack(int gameId) {
    return getRestClient().get(API + "puppacks/" + gameId, PupPackRepresentation.class);
  }

  public PupPackRepresentation getMenuPupPack() {
    return getRestClient().get(API + "puppacks/menu", PupPackRepresentation.class);
  }

  public boolean delete(int gameId) {
    return getRestClient().delete(API + "puppacks/" + gameId);
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

  public JobDescriptor option(int gameId, String option) throws Exception {
    CommandOption o = new CommandOption();
    o.setCommand(option);
    return getRestClient().post(API + "puppacks/option/" + gameId, o, JobDescriptor.class);
  }

  public UploadDescriptor uploadPupPack(File file, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "puppacks/upload";
      HttpEntity upload = createUpload(file, -1, null, AssetType.PUP_PACK, listener);
      ResponseEntity<UploadDescriptor> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, upload, UploadDescriptor.class);
      finalizeUpload(upload);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("PUP pack upload failed: " + e.getMessage(), e);
      throw e;
    }
  }
}
