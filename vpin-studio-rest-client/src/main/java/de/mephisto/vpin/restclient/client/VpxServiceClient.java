package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.AssetType;
import de.mephisto.vpin.restclient.FileUploadProgressListener;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.POVRepresentation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
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
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  VpxServiceClient(VPinStudioClient client) {
    super(client);
  }

  public void playGame(int id) {
    try {
      getRestClient().put(API + "vpx/play/" + id, new HashMap<>());
    } catch (Exception e) {
      LOG.error("Failed to start game " + id + ": " + e.getMessage(), e);
    }
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
    } catch (Exception e) {
      LOG.error("Failed to set preferences: " + e.getMessage(), e);
    }
    return false;
  }


  public POVRepresentation createPOV(int gameId) throws Exception {
    try {
      return getRestClient().post(API + "vpx/pov/" + gameId, new HashMap<>(), POVRepresentation.class);
    } catch (Exception e) {
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

  public void saveTableSource(GameRepresentation game, String sources) {
    try {
      Map<String, Object> data = new HashMap<>();
      data.put("source", Base64.getEncoder().encodeToString(sources.getBytes()));
      getRestClient().put(API + "vpx/sources/" + game.getId(), data);
    } catch (Exception e) {
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
      } catch (IOException e) {
        LOG.error("Failed to create temp file for script: " + e.getMessage(), e);
      }
    }
    return null;
  }

  public JobExecutionResult uploadPov(File file, String uploadType, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "vpx/pov/upload";
      ResponseEntity<JobExecutionResult> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, createUpload(file, gameId, uploadType, AssetType.POV, listener), JobExecutionResult.class);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("POV upload failed: " + e.getMessage(), e);
      throw e;
    }
  }
}
