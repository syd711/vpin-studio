package de.mephisto.vpin.restclient.directb2s;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/*********************************************************************************************************************
 * DirectB2S
 ********************************************************************************************************************/
public class BackglassServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public BackglassServiceClient(VPinStudioClient client) {
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

  public DirectB2SData getDirectB2SData(int emulatorId, String name) {
    name = URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");
    return getRestClient().get(API + "directb2s/" + emulatorId + "/" + name, DirectB2SData.class);
  }

  public List<DirectB2S> getBackglasses() {
    return Arrays.asList(getRestClient().get(API + "directb2s", DirectB2S[].class));
  }

  public DirectB2ServerSettings getServerSettings(int emuId) {
    return getRestClient().get(API + "directb2s/serversettings/" + emuId, DirectB2ServerSettings.class);
  }

  public DirectB2STableSettings getTableSettings(int gameId) {
    return getRestClient().get(API + "directb2s/tablesettings/" + gameId, DirectB2STableSettings.class);
  }

  public DirectB2ServerSettings saveServerSettings(int emuId, DirectB2ServerSettings settings) throws Exception {
    try {
      return getRestClient().post(API + "directb2s/serversettings/" + emuId, settings, DirectB2ServerSettings.class);
    } catch (Exception e) {
      LOG.error("Failed to save b2s server settings: " + e.getMessage(), e);
      throw e;
    }
  }

  public DirectB2STableSettings saveTableSettings(int gameId, DirectB2STableSettings settings) throws Exception {
    try {
      return getRestClient().post(API + "directb2s/tablesettings/" + gameId, settings, DirectB2STableSettings.class);
    } catch (Exception e) {
      LOG.error("Failed to save b2s table settings: " + e.getMessage(), e);
      throw new Exception("Table not supported.");
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
