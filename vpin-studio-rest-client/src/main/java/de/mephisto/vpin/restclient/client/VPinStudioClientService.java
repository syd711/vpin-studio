package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.altsound.AltSoundServiceClient;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClientService.class);

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

  protected MultipartBodyBuilder createUploadBuilder(File file, int gameId, String uploadType, AssetType assetType, FileUploadProgressListener listener) {
    // ProgressableFileSystemResource resource = new ProgressableFileSystemResource(file, listener);
    ProgressableInputStreamResource resource = new ProgressableInputStreamResource(file, listener);

    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("file", resource).filename(file.getName());
    builder.part("objectId", gameId);
    if (uploadType != null) {
      builder.part("uploadType", uploadType);
    }
    builder.part("assetType", assetType.name());

    return builder;
  }

  protected MultiValueMap<String, Object> createUploadForm(File file, int gameId, String uploadType, AssetType assetType, FileUploadProgressListener listener) {
    // MyResource resource = new MyResource(file, listener);

    // Create a resource for the file
    FileSystemResource resource = new FileSystemResource(file);
    long totalBytes = file.length();

    // Create a Flux<DataBuffer> for the file and wrap it to track progress
    Flux<DataBuffer> dataBufferFlux = DataBufferUtils.read(resource, new DefaultDataBufferFactory(), 4096);
    Flux<DataBuffer> dataBufferWithProgress = ProgressTrackingDataBuffer.wrapWithProgress(dataBufferFlux, totalBytes, listener);

    MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
    formData.add("file", BodyInserters.fromDataBuffers(dataBufferWithProgress));
    // formData.add("file", resource);
    formData.add("objectId", gameId);
    if (uploadType != null) {
      formData.add("uploadType", uploadType);
    }
    formData.add("assetType", assetType.name());

    return formData;
  }

  protected <T> Mono<T> webClientPost(String url, /*MultipartBodyBuilder builder*/ MultiValueMap<String, Object> formData, Class<T> responseType) {
    return WebClient.builder().build().post()
            .uri(url)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(formData))
            .retrieve()
            .bodyToMono(responseType)
            .doOnCancel(() -> LOG.warn("Upload aborted due to cancellation request"))
            .doOnError(throwable -> {
              if (!(throwable instanceof java.util.concurrent.CancellationException)) {
                  LOG.error("Failed to upload using the WebClient: {}", throwable.getMessage(), throwable);
              }
            })
            .doFinally(signal -> finalizeUploadWebClient(formData));
    // .body(BodyInserters.fromMultipartData(builder.build()))
    // .bodyValue(builder.build())
  }

  protected RestTemplate createUploadTemplate() {
    SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
    rf.setBufferRequestBody(false);
    return new RestTemplate(rf);
  }

  public static void finalizeUpload(HttpEntity upload) {
    Map<String, Object> data = (Map<String, Object>) upload.getBody();
    List fields = (List) data.get("file");
    ProgressableFileSystemResource resource = (ProgressableFileSystemResource) fields.get(0);
    resource.close();
  }

  protected static void finalizeUploadWebClient(MultiValueMap<String, Object> formData) {
//    MyResource resource = (MyResource) formData.get("file").get(0);
//    resource.close();
  }

  protected static void finalizeUploadWebClient2(MultipartBodyBuilder builder) {
    ProgressableInputStreamResource resource = (ProgressableInputStreamResource) builder.build().get("file").get(0).getBody();
    resource.close();
  }
}
