package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import de.mephisto.vpin.restclient.util.ProgressableFileSystemResource;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Map;

public class VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClientService.class);

  public final static String API = VPinStudioClient.API;

  public VPinStudioClient client;
  private final CloseableHttpClient httpClient;

  public VPinStudioClientService(VPinStudioClient client) {
    this.client = client;

    this.httpClient = HttpClients.createDefault();
  }

  protected RestClient getRestClient() {
    return client.getRestClient();
  }

  protected HttpEntity createUpload(File file, int gameId, String uploadType, AssetType assetType, FileUploadProgressListener listener) throws Exception {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    return createUpload(map, file, gameId, uploadType, assetType, listener);
  }

  protected HttpEntity createUpload(LinkedMultiValueMap<String, Object> map, File file, int gameId, String uploadType, AssetType assetType, FileUploadProgressListener listener) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String boundary = Long.toHexString(System.currentTimeMillis());
    headers.set("Content-Type", "multipart/form-data; boundary=" + boundary);
    ProgressableFileSystemResource rsr = new ProgressableFileSystemResource(file, listener);

    map.add("file", rsr);
    map.add("objectId", gameId);
    map.add("uploadType", uploadType);
    map.add("assetType", assetType.name());
    return new HttpEntity<>(map, headers);
  }

  protected RestTemplate createUploadTemplate() {
    HttpComponentsClientHttpRequestFactory rf = new HttpComponentsClientHttpRequestFactory(httpClient);
    rf.setBufferRequestBody(false);
    return new RestTemplate(rf);
  }

  public static void finalizeUpload(HttpEntity upload) {
    Map<String, Object> data = (Map<String, Object>) upload.getBody();
    List fields = (List) data.get("file");
    ProgressableFileSystemResource resource = (ProgressableFileSystemResource) fields.get(0);
    resource.close();
  }

  public void closeHttpClient() {
    // Properly close the HttpClient when done
    if (httpClient != null) {
      try {
        httpClient.close();
      } catch (Exception e) {
        LOG.error("Failed to close HttpClient: " + e.getMessage(), e);
      }
    }
  }
}
