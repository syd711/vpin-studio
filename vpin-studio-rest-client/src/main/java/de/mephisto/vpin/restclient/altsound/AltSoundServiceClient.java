package de.mephisto.vpin.restclient.altsound;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

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

  public UploadDescriptor uploadAltSound(File file, int emulatorId, FileUploadProgressListener listener, Supplier<Boolean> isCancelled) throws Exception {
    RestTemplate restTemplate = createAbortableUploadTemplate();

    try {
      String url = getRestClient().getBaseUrl() + API + "altsound/upload";
      HttpPost request = new HttpPost(url);

      // Start a new thread to monitor the cancellation status
      Thread cancellationThread = new Thread(() -> {
        while (!Thread.currentThread().isInterrupted()) {
          if (isCancelled.get()) {
            request.abort();
            LOG.warn("Upload aborted due to cancellation request");
            break;
          }
        }
      });
      cancellationThread.start();

      HttpEntity upload = createUpload(file, emulatorId, null, AssetType.ALT_SOUND, listener);
      UploadDescriptor body = restTemplate.exchange(url, HttpMethod.POST, upload, UploadDescriptor.class).getBody();

      // HttpEntity upload = createUpload(file, emulatorId, null, AssetType.ALT_SOUND, listener);
      // UploadDescriptor body = createUploadTemplate().exchange(url, HttpMethod.POST, upload, UploadDescriptor.class).getBody();
      finalizeUpload(upload);
      return body;
    } catch (Exception e) {
      LOG.error("ALT sound upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public Future<UploadDescriptor> uploadAltSoundFuture(File file, int emulatorId, FileUploadProgressListener listener) throws Exception {
    Callable<UploadDescriptor> task = () -> {
      // Create a Supplier<Boolean> that checks the cancellation status
      Supplier<Boolean> isCancelled = () -> Thread.currentThread().isInterrupted();

      return this.uploadAltSound(file, emulatorId, listener, isCancelled);
    };

    return executor.submit(task);
  }

  public String getAudioUrl(AltSound altSound, int emuId, String item) {
    return "altsound/stream/" + emuId + "/" + altSound.getName() + "/" + item;
  }
}
