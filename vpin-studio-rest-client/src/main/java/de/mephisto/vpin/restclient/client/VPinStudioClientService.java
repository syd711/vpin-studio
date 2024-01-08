package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import de.mephisto.vpin.restclient.util.ProgressableFileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

public class VPinStudioClientService {
  public final static String API = VPinStudioClient.API;

  public VPinStudioClient client;

  public VPinStudioClientService(VPinStudioClient client) {
    this.client = client;
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
    SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
    rf.setBufferRequestBody(false);
    return new RestTemplate(rf);
  }
}
