package de.mephisto.vpin.restclient.vpx;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.representations.POVRepresentation;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/*********************************************************************************************************************
 * VPX
 ********************************************************************************************************************/
public class VpxServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public VpxServiceClient(VPinStudioClient client) {
    super(client);
  }

  public POVRepresentation getPOV(int gameId) {
    Map<String, Object> povData = getRestClient().get(API + "vpx/pov/" + gameId, Map.class);
    return new POVRepresentation(povData);
  }

  public boolean setPOVPreference(int gameId, POVRepresentation pov, String property, Object value) {
    try {
      if (pov == null) {
        return true;
      }
      Object existingValue = pov.getValue(property);
      if (!existingValue.equals(value)) {
        Map<String, Object> values = new HashMap<>();
        values.put("property", property);
        values.put("value", value);
        LOG.info("Update POV property " + property + " to " + value);
        pov.getValues().put(property, value);
        return getRestClient().put(API + "vpx/pov/" + gameId, values);
      }
      return true;
    }
    catch (Exception e) {
      LOG.error("Failed to set preferences: " + e.getMessage(), e);
    }
    return false;
  }

  public int setNvOffset(int gameId, int offset) throws Exception {
    try {
      Map<String, Object> values = new HashMap<>();
      values.put("nvOffset", offset);
      LOG.info("Update nvoffset to " + offset);
      return getRestClient().put(API + "vpx/nvoffset/" + gameId, values, Integer.class);
    }
    catch (Exception e) {
      LOG.error("Failed to set nvoffset: " + e.getMessage(), e);
      throw e;
    }
  }


  public POVRepresentation createPOV(int gameId) throws Exception {
    try {
      return getRestClient().post(API + "vpx/pov/" + gameId, new HashMap<>(), POVRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Failed to create POV representation: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean deletePOV(int gameId) {
    return getRestClient().delete(API + "vpx/pov/" + gameId);
  }

  public String getTableSource(GameRepresentation game) {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "vpx/sources/" + game.getId(), String.class);
  }

  public String getVpxFile() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "vpx/vpinballx", String.class);
  }

  public TableInfo getTableInfo(GameRepresentation game) {
    return getRestClient().get(API + "vpx/tableinfo/" + game.getId(), TableInfo.class);
  }

  public String getCheckSum(GameRepresentation game) {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "vpx/checksum/" + game.getId(), String.class);
  }

  public void saveTableSource(GameRepresentation game, String sources) {
    try {
      Map<String, Object> data = new HashMap<>();
      data.put("source", Base64.getEncoder().encodeToString(sources.getBytes()));
      getRestClient().put(API + "vpx/sources/" + game.getId(), data);
    }
    catch (Exception e) {
      LOG.error("Failed to save script data: " + e.getMessage(), e);
    }
  }

  public File getTableScript(GameRepresentation game) {
    final RestTemplate restTemplate = new RestTemplate();
    String src = restTemplate.getForObject(getRestClient().getBaseUrl() + API + "vpx/script/" + game.getId(), String.class);
    if (!StringUtils.isEmpty(src)) {
      try {
        File tmp = File.createTempFile(game.getGameDisplayName() + "-script-src", ".txt");
        tmp.deleteOnExit();

        Path path = Paths.get(tmp.toURI());
        byte[] strToBytes = src.getBytes();
        Files.write(path, strToBytes);

        return tmp;
      }
      catch (IOException e) {
        LOG.error("Failed to create temp file for script: " + e.getMessage(), e);
      }
    }
    return null;
  }

  public UploadDescriptor uploadMusic(File file, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "vpx/music/upload";
      HttpEntity upload = createUpload(file, -1, null, AssetType.MUSIC, listener);
      ResponseEntity<UploadDescriptor> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, upload, UploadDescriptor.class);
      finalizeUpload(upload);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("Music upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public UploadDescriptor uploadPov(File file, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "vpx/pov/upload";
      HttpEntity upload = createUpload(file, gameId, null, AssetType.POV, listener);
      ResponseEntity<UploadDescriptor> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, upload, UploadDescriptor.class);
      finalizeUpload(upload);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("POV upload failed: " + e.getMessage(), e);
      throw e;
    }
  }
}
