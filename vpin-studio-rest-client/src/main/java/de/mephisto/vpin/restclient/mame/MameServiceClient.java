package de.mephisto.vpin.restclient.mame;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;

/*********************************************************************************************************************
 * Mame
 ********************************************************************************************************************/
public class MameServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MameServiceClient.class);

  public MameServiceClient(VPinStudioClient client) {
    super(client);
  }

  public MameOptions getOptions(String name) {
    return getRestClient().get(API + "mame/options/" + name, MameOptions.class);
  }

  public File getDmdDeviceIni() {
    return getRestClient().get(API + "mame/dmddevice.ini", File.class);
  }

  public boolean runSetup() {
    return getRestClient().get(API + "mame/setup", Boolean.class);
  }

  public boolean runFlexSetup() {
    return getRestClient().get(API + "mame/flexsetup", Boolean.class);
  }

  public boolean clearCache() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "mame/clearcache", Boolean.class);
  }

  public boolean clearCacheFor(@Nullable String rom) {
    if (!StringUtils.isEmpty(rom)) {
      final RestTemplate restTemplate = new RestTemplate();
      return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "mame/clearcachefor/" + rom, Boolean.class);
    }
    return false;
  }

  public UploadDescriptor uploadRom(int emuId, File file, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "mame/upload/rom/" + emuId;
      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add("emuId", emuId);
      ResponseEntity<UploadDescriptor> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, createUpload(map, file, -1, null, AssetType.TABLE, listener), UploadDescriptor.class);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("Rom upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public UploadDescriptor uploadCfg(int emuId, File file, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "mame/upload/cfg/" + emuId;
      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add("emuId", emuId);
      ResponseEntity<UploadDescriptor> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, createUpload(map, file, -1, null, AssetType.CFG, listener), UploadDescriptor.class);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("Rom upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public UploadDescriptor uploadNvRam(int emuId, File file, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "mame/upload/nvram/" + emuId;
      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add("emuId", emuId);
      ResponseEntity<UploadDescriptor> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, createUpload(map, file, -1, null, AssetType.NV, listener), UploadDescriptor.class);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("Rom upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public Boolean deleteSettings(@NonNull String rom) {
    return getRestClient().delete(API + "mame/options/" + rom);
  }

  public MameOptions saveOptions(MameOptions options) throws Exception {
    return getRestClient().post(API + "mame/options/", options, MameOptions.class);
  }
}
