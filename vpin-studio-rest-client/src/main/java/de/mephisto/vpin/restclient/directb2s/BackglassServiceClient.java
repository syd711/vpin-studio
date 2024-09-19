package de.mephisto.vpin.restclient.directb2s;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


  public boolean clearCache() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "directb2s/clearcache", Boolean.class);
  }

  public InputStream getDirectB2sBackground(DirectB2SData directB2S) throws IOException {
    String url = getRestClient().getBaseUrl() + API + "directb2s/background/" + directB2S.getEmulatorId() + "/" 
      + URLEncoder.encode(URLEncoder.encode(directB2S.getFilename(), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    return new URL(url).openStream();
  }
  public InputStream getDirectB2sDmd(DirectB2SData directB2S) throws IOException {
    String url = getRestClient().getBaseUrl() + API + "directb2s/dmdimage/" + directB2S.getEmulatorId() + "/" 
    + URLEncoder.encode(URLEncoder.encode(directB2S.getFilename(), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    return new URL(url).openStream();
  }

  public DirectB2SData getDirectB2SData(DirectB2S directB2S) {
    return getRestClient().post(API + "directb2s/get", directB2S, DirectB2SData.class);
  }

  public boolean deleteBackglass(DirectB2S directB2S) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("emulatorId", directB2S.getEmulatorId());
    params.put("fileName", directB2S.getFileName());
    return getRestClient().post(API + "directb2s/delete", directB2S, Boolean.class);
  }
 
  public boolean renameBackglass(DirectB2S directB2S, String newName) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("newName", newName);
    params.put("emulatorId", directB2S.getEmulatorId());
    params.put("fileName", directB2S.getFileName());
    return getRestClient().put(API + "directb2s", params, Boolean.class);
  }

  public boolean duplicateBackglass(DirectB2S directB2S) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("duplicate", true);
    params.put("emulatorId", directB2S.getEmulatorId());
    params.put("fileName", directB2S.getFileName());
    return getRestClient().put(API + "directb2s", params, Boolean.class);
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
      throw new Exception("Table not supported (" + settings.getRom() + ")");
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

  public UploadDescriptor uploadDirectB2SFile(File file, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "directb2s/upload";
      HttpEntity<?> upload = createUpload(file, gameId, null, AssetType.DIRECTB2S, listener);
      ResponseEntity<UploadDescriptor> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, upload, UploadDescriptor.class);
      finalizeUpload(upload);
      return exchange.getBody();
    } 
    catch (Exception e) {
      LOG.error("Directb2s upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean uploadDMDImage(DirectB2S directb2s, File file) {
    try {
      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add("emuid", directb2s.getEmulatorId());
      map.add("filename", directb2s.getFileName());
      String url = getRestClient().getBaseUrl() + API + "directb2s/uploadDmdImage";
      HttpEntity<?> upload = createUpload(map, file, -1, null, AssetType.DIRECTB2S, null);

      //new RestTemplate().exchange(url, HttpMethod.POST, upload, Boolean.class);
      createUploadTemplate().exchange(url, HttpMethod.POST, upload, Boolean.class);
      finalizeUpload(upload);
      return true;
    } 
    catch (Exception e) {
      LOG.error("Directb2s upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean removeDMDImage(DirectB2S directb2s) throws Exception {
    return getRestClient().post(API + "directb2s/removeDmdImage", directb2s, Boolean.class);
  }
}
