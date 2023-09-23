package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.*;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;

/*********************************************************************************************************************
 * DirectB2S
 ********************************************************************************************************************/
public class BackglassServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  BackglassServiceClient(VPinStudioClient client) {
    super(client);
  }

  public ByteArrayInputStream getDefaultPicture(GameRepresentation game) {
    byte[] bytes = getRestClient().readBinary(API + "assets/defaultbackground/" + game.getId());
    if (bytes == null) {
      LOG.error("Failed to read image, using empty bytes.");
      bytes = new byte[]{};
    }
    return new ByteArrayInputStream(bytes);
  }

  public DirectB2SData getDirectB2SData(int gameId) {
    return getRestClient().get(API + "directb2s/" + gameId, DirectB2SData.class);
  }

  public DirectB2ServerSettings getServerSettings() {
    return getRestClient().get(API + "directb2s/serversettings", DirectB2ServerSettings.class);
  }

  public DirectB2STableSettings getTableSettings(int gameId) {
    return getRestClient().get(API + "directb2s/tablesettings/" + gameId, DirectB2STableSettings.class);
  }

  public DirectB2ServerSettings saveServerSettings(DirectB2ServerSettings settings) throws Exception {
    try {
      return getRestClient().post(API + "directb2s/serversettings", settings, DirectB2ServerSettings.class);
    } catch (Exception e) {
      LOG.error("Failed to save b2s server settings: " + e.getMessage(), e);
      throw e;
    }
  }

  public DirectB2STableSettings saveTableSettings(DirectB2STableSettings settings) throws Exception {
    try {
      return getRestClient().post(API + "directb2s/tablesettings", settings, DirectB2STableSettings.class);
    } catch (Exception e) {
      LOG.error("Failed to save b2s table settings: " + e.getMessage(), e);
      throw e;
    }
  }

  public GameRepresentation saveGame(GameRepresentation game) throws Exception {
    try {
      return getRestClient().post(API + "games/save", game, GameRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save game: " + e.getMessage(), e);
      throw e;
    }
  }

  public JobExecutionResult uploadDirectB2SFile(File file, String uploadType, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "directb2s/upload";
      ResponseEntity<JobExecutionResult> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, createUpload(file, gameId, uploadType, AssetType.DIRECT_B2S, listener), JobExecutionResult.class);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("Directb2s upload failed: " + e.getMessage(), e);
      throw e;
    }
  }
}
