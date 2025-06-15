package de.mephisto.vpin.restclient.altsound;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.system.FileInfo;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/*********************************************************************************************************************
 * Alt Sound
 ********************************************************************************************************************/
public class AltSoundServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(AltSoundServiceClient.class);

  public AltSoundServiceClient(VPinStudioClient client) {
    super(client);
  }

  public AltSound saveAltSound(int gameId, AltSound altSound) throws Exception {
    return getRestClient().post(API + "altsound/save/" + gameId, altSound, AltSound.class);
  }

  public AltSound getAltSound(int gameId) {
    return getRestClient().get(API + "altsound/" + gameId, AltSound.class);
  }

  public FileInfo getAltSoundFolderInfo(int gameId) {
    return getRestClient().get(API + "altsound/" + gameId + "/fileinfo", FileInfo.class);
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

  public UploadDescriptor uploadAltSound(File file, int emulatorId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "altsound/upload";
      HttpEntity upload = createUpload(file, emulatorId, null, AssetType.ALT_SOUND, listener);
      ResponseEntity<UploadDescriptor> exchange = new RestTemplate().exchange(url, HttpMethod.POST, upload, UploadDescriptor.class);
      finalizeUpload(upload);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("ALT sound upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public String getAudioUrl(AltSound altSound, int emuId, String item) {
    return "altsound/stream/" + emuId + "/" + altSound.getName() + "/" + item;
  }
}
