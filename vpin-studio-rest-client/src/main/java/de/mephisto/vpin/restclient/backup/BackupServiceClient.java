package de.mephisto.vpin.restclient.backup;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.HashMap;

/*********************************************************************************************************************
 * Backups
 ********************************************************************************************************************/
public class BackupServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public BackupServiceClient(VPinStudioClient client) {
    super(client);
  }

  private final static ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }


  public String backup() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.postForObject(getRestClient().getBaseUrl() + API + "backup/create", new HashMap<>(), String.class);
  }

  public boolean restore(@NonNull File file, @NonNull BackupDescriptor backupDescriptor) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "backup/restore";
      HttpEntity upload = createUpload(file, -1, null, null, null);
      LinkedMultiValueMap<String, Object> map = (LinkedMultiValueMap<String, Object>) upload.getBody();

      String backupDescriptorJson = objectMapper.writeValueAsString(backupDescriptor);
      map.add("backupDescriptor", backupDescriptorJson);
      new RestTemplate().exchange(url, HttpMethod.POST, upload, Boolean.class);
      finalizeUpload(upload);
      return true;
    }
    catch (Exception e) {
      LOG.error("Backup upload failed: " + e.getMessage(), e);
      throw e;
    }
  }
}
