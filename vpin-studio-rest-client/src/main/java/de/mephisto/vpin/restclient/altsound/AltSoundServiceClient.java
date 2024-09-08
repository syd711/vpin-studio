package de.mephisto.vpin.restclient.altsound;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/*********************************************************************************************************************
 * Alt Sound
 ********************************************************************************************************************/
public class AltSoundServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundServiceClient.class);

  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private Disposable uploadDisposable = null;

  public AltSoundServiceClient(VPinStudioClient client) {
    super(client);
  }

  public AltSound saveAltSound(int gameId, AltSound altSound) throws Exception {
    return getRestClient().post(API + "altsound/save/" + gameId, altSound, AltSound.class);
  }

  public AltSound getAltSound(int gameId) {
    return getRestClient().get(API + "altsound/" + gameId, AltSound.class);
  }

  public AltSound restoreAltSound(int gameId) {
    return getRestClient().get(API + "altsound/restore/" + gameId, AltSound.class);
  }

  public boolean clearCache() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "altsound/clearcache", Boolean.class);
  }

  public boolean delete(int gameId) {
    return getRestClient().delete(API + "altsound/" + gameId);
  }

  public UploadDescriptor uploadAltSound(File file, int emulatorId, FileUploadProgressListener listener) {
    try {
      String url = getRestClient().getBaseUrl() + API + "altsound/upload";

      MultiValueMap<String, Object> formData = createUploadForm(file, emulatorId, null, AssetType.ALT_SOUND, listener);
      // MultipartBodyBuilder builder = createUploadBuilder(file, emulatorId, null, AssetType.ALT_SOUND, listener);
      Mono<UploadDescriptor> responseMono = webClientPost(url, formData, /*builder,*/ UploadDescriptor.class);
      uploadDisposable = responseMono.subscribe();

      return responseMono.block();
    } catch (Exception e) {
      if (e.getCause() instanceof InterruptedException) {
        LOG.error("ALT sound upload has likely been cancelled: {}", e.getMessage());
      } else {
        LOG.error("ALT sound upload failed: {}", e.getMessage(), e);
      }

      throw e;
    }
  }

  public Future<UploadDescriptor> uploadAltSoundFuture(File file, int emulatorId, FileUploadProgressListener listener) throws Exception {
    Callable<UploadDescriptor> task = () -> {
      try {
        UploadDescriptor foo = this.uploadAltSound(file, emulatorId, listener);
        return foo;
      } catch (Exception e) {
        if (uploadDisposable != null && !uploadDisposable.isDisposed()) {
          uploadDisposable.dispose();
        }
        return null;
      }
    };

    return executor.submit(task);
  }

  public String getAudioUrl(AltSound altSound, int emuId, String item) {
    return "altsound/stream/" + emuId + "/" + altSound.getName() + "/" + item;
  }
}
