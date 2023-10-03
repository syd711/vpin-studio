package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/*********************************************************************************************************************
 * Alt Sound
 ********************************************************************************************************************/
public class AltSoundServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundServiceClient.class);

  AltSoundServiceClient(VPinStudioClient client) {
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

  public boolean isAltSoundEnabled(int gameId) {
    return getRestClient().get(API + "altsound/enabled/" + gameId, Boolean.class);
  }

  public boolean setAltSoundEnabled(int gameId, boolean b) {
    return getRestClient().get(API + "altsound/set/" + gameId + "/" + b, Boolean.class);
  }

  public JobExecutionResult uploadAltSound(File file, String uploadType, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "altsound/upload";
      ResponseEntity<JobExecutionResult> exchange = new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, gameId, uploadType, AssetType.ALT_SOUND, listener), JobExecutionResult.class);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("ALT sound upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean clearCache() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "altsound/clearcache", Boolean.class);
  }

  public String getAudioUrl(AltSound altSound, int emuId, String item) {
    return "altsound/stream/" + emuId + "/" + altSound.getName() + "/" + item;
  }
}
