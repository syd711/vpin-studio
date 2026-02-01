package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import de.mephisto.vpin.restclient.util.ProgressableFileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;
import java.util.Map;

public class VPinStudioClientService {
  public final static String API = VPinStudioClient.API;

  public VPinStudioClient client;

  public VPinStudioClientService(VPinStudioClient client) {
    this.client = client;
  }

  protected RestClient getRestClient() {
    return client.getRestClient();
  }

  protected HttpEntity<MultiValueMap<String, Object>> createUpload(File file, int objectId, UploadType uploadType, AssetType assetType, FileUploadProgressListener listener) throws Exception {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    return createUpload(map, file, objectId, uploadType, assetType, listener);
  }

  protected HttpEntity<MultiValueMap<String, Object>> createUpload(MultiValueMap<String, Object> map, File file, int objectId, UploadType uploadType, AssetType assetType, FileUploadProgressListener listener) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String boundary = Long.toHexString(System.currentTimeMillis());
    headers.set("Content-Type", "multipart/form-data; boundary=" + boundary);
    ProgressableFileSystemResource rsr = new ProgressableFileSystemResource(file, listener);

    map.add("file", rsr);
    map.add("objectId", objectId);
    map.add("uploadType", uploadType);
    map.add("assetType", assetType != null ? assetType.name() : null);
    return new HttpEntity<>(map, headers);
  }

  protected RestTemplate createUploadTemplate() {
    SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
    rf.setBufferRequestBody(false);
    return new RestTemplate(rf);
  }

  public static void finalizeUpload(HttpEntity<MultiValueMap<String, Object>> upload) {
    try {
      Map<String, List<Object>> data = upload.getBody();
      List<?> fields = data.get("file");
      ProgressableFileSystemResource resource = (ProgressableFileSystemResource) fields.get(0);
      resource.close();
    }
    catch (Exception e) {
      //ignore
    }
  }
}
