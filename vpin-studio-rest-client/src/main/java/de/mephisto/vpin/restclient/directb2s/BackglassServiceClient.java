package de.mephisto.vpin.restclient.directb2s;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import de.mephisto.vpin.restclient.util.ReturnMessage;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

import java.io.*;
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

  public String getDefaultPictureUrl(@NonNull GameRepresentation game) {
    return getRestClient().getBaseUrl() + API + "assets/defaultbackground/" + game.getId();
  }

  public int getGameId(int emulatorId, String fileName) {
    Map<String, Object> params = new HashMap<>();
    params.put("emulatorId", emulatorId);
    params.put("fileName", fileName);
    return getRestClient().post(API + "directb2s/gameId", params, Integer.class);
  }

  public DirectB2SData getDirectB2SData(int gameId) {
    return getRestClient().get(API + "directb2s/" + gameId, DirectB2SData.class);
  }

  public DirectB2SData getDirectB2SData(int emulatorId, String fileName) {
    Map<String, Object> params = new HashMap<>();
    params.put("emulatorId", emulatorId);
    params.put("fileName", fileName);
    return getRestClient().post(API + "directb2s/get", params, DirectB2SData.class);
  }

  public DirectB2SDetail getDirectB2SDetail(int emulatorId, String fileName, int gameId) {
    Map<String, Object> params = new HashMap<>();
    params.put("emulatorId", emulatorId);
    params.put("fileName", fileName);
    params.put("gameId", gameId);
    return getRestClient().post(API + "directb2s/detail", params, DirectB2SDetail.class);
  }

  public DirectB2S getDirectB2S(int gameId) {
    return getRestClient().get(API + "directb2s/" + gameId + "/versions", DirectB2S.class);
  }

  public List<DirectB2S> getBackglasses() {
    return Arrays.asList(getRestClient().get(API + "directb2s", DirectB2S[].class));
  }

  public DirectB2S reloadDirectB2S(int emulatorId, String fileName) {
    Map<String, Object> params = new HashMap<>();
    params.put("emulatorId", emulatorId);
    params.put("fileName", fileName);
    return getRestClient().post(API + "directb2s/versions", params, DirectB2S.class);
  }

  public boolean clearCache() {
    return getRestClient().get(API + "directb2s/clearcache", Boolean.class);
  }

  //--------------------------------
  // DOWNLOAD IMAGES

  public String getDirectB2sBackgroundUrl(int gameId) {
    return getRestClient().getBaseUrl() + API + "directb2s/background/" + gameId;
  }

  public String getDirectB2sBackgroundUrl(int emulatorId, String filename) {
    return getRestClient().getBaseUrl() + API + "directb2s/background/" + emulatorId + "/"
        + URLEncoder.encode(URLEncoder.encode(filename, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
  }

  public InputStream getDirectB2sBackground(DirectB2SData directB2S) throws IOException {
    String url = getDirectB2sBackgroundUrl(directB2S.getEmulatorId(), directB2S.getFilename());
    return new URL(url).openStream();
  }

  public String getDirectB2sDmdUrl(int gameId) {
    return getRestClient().getBaseUrl() + API + "directb2s/dmdimage/" + gameId;
  }

  public String getDirectB2sDmdUrl(int emulatorId, String filename) {
    return getRestClient().getBaseUrl() + API + "directb2s/dmdimage/" + emulatorId + "/"
        + URLEncoder.encode(URLEncoder.encode(filename, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
  }

  public InputStream getDirectB2sDmd(DirectB2SData directB2S) throws IOException {
    String url = getDirectB2sDmdUrl(directB2S.getEmulatorId(), directB2S.getFilename());
    return new URL(url).openStream();
  }

  public String getDirectB2sPreviewBackgroundUrl(int gameId, boolean includeFrame) {
    return getRestClient().getBaseUrl() + API + "directb2s/previewBackground/" + gameId + ".png"
        + (includeFrame ? "?includeFrame=true" : "");
  }

  public String getDirectB2sPreviewBackgroundUrl(int emulatorId, String filename, boolean includeFrame) {
    return getRestClient().getBaseUrl() + API + "directb2s/previewBackground/" + emulatorId + "/"
        + URLEncoder.encode(URLEncoder.encode(filename, StandardCharsets.UTF_8), StandardCharsets.UTF_8) + ".png"
        + (includeFrame ? "?includeFrame=true" : "");
  }

  public InputStream getDirectB2sPreviewBackground(DirectB2SData directB2S, boolean includeFrame) throws IOException {
    String url = getDirectB2sPreviewBackgroundUrl(directB2S.getEmulatorId(), directB2S.getFilename(), includeFrame);
    return new URL(url).openStream();
  }


  //--------------------------------
  // BACKGLASS OPERATIONS

  public boolean deleteBackglass(int emulatorId, String fileName) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("emulatorId", emulatorId);
    params.put("fileName", fileName);
    return getRestClient().post(API + "directb2s/delete", params, Boolean.class);
  }

  public DirectB2S renameBackglass(int emulatorId, String fileName, String newName) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("newName", newName);
    params.put("emulatorId", emulatorId);
    params.put("fileName", fileName);
    return getRestClient().put(API + "directb2s", params, DirectB2S.class);
  }

  public DirectB2S setBackglassAsDefault(int emulatorId, String fileName) {
    Map<String, Object> params = new HashMap<>();
    params.put("setVersionAsDefault", fileName);
    params.put("emulatorId", emulatorId);
    params.put("fileName", fileName);
    try {
      return getRestClient().put(API + "directb2s", params, DirectB2S.class);
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public DirectB2S disableBackglass(int emulatorId, String fileName) {
    Map<String, Object> params = new HashMap<>();
    params.put("disable", true);
    params.put("emulatorId", emulatorId);
    params.put("fileName", fileName);
    try {
      return getRestClient().put(API + "directb2s", params, DirectB2S.class);
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public DirectB2S deleteBackglassVersion(int emulatorId, String fileName) {
    Map<String, Object> params = new HashMap<>();
    params.put("deleteVersion", true);
    params.put("emulatorId", emulatorId);
    params.put("fileName", fileName);
    try {
      return getRestClient().put(API + "directb2s", params, DirectB2S.class);
    }
    catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public GameRepresentation saveGame(GameRepresentation game) throws Exception {
    try {
      return getRestClient().post(API + "games/save", game, GameRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save game: " + e.getMessage(), e);
      throw e;
    }
  }

  public File getBackglassServerFolder() {
    DirectB2ServerSettings serverSettings = getServerSettings();
    return serverSettings.getBackglassServerFolder() != null ?
        new File(serverSettings.getBackglassServerFolder()) : null;
  }

  //--------------------------------
  // MANAGE SETTINGS

  public DirectB2ServerSettings getServerSettings() {
    return getRestClient().get(API + "directb2s/serversettings", DirectB2ServerSettings.class);
  }

  public DirectB2STableSettings getTableSettings(int gameId) {
    return getRestClient().get(API + "directb2s/tablesettings/" + gameId, DirectB2STableSettings.class);
  }

  public DirectB2ServerSettings saveServerSettings(DirectB2ServerSettings settings) throws Exception {
    try {
      return getRestClient().post(API + "directb2s/serversettings", settings, DirectB2ServerSettings.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save b2s server settings: " + e.getMessage(), e);
      throw e;
    }
  }

  public DirectB2STableSettings saveTableSettings(int gameId, DirectB2STableSettings settings) {
    try {
      return getRestClient().post(API + "directb2s/tablesettings/" + gameId, settings, DirectB2STableSettings.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save b2s table settings: " + e.getMessage(), e);
      throw e;
    }
  }

  //--------------------------------

  public UploadDescriptor uploadDirectB2SFile(File file, int gameId, boolean append, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "directb2s/upload";
      String uploadType = append ? UploadType.uploadAndAppend.name() : UploadType.uploadAndImport.name();
      HttpEntity<?> upload = createUpload(file, gameId, uploadType, AssetType.DIRECTB2S, listener);
      ResponseEntity<UploadDescriptor> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, upload, UploadDescriptor.class);
      finalizeUpload(upload);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("Directb2s upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  //--------------------------------
  // DMD Image management

  public boolean uploadDMDImage(int emulatorId, String fileName, File file) {
    return uploadFile(emulatorId, fileName, "directb2s/uploadDmdImage", file, Boolean.class);
  }

  public boolean removeDMDImage(int emulatorId, String fileName) throws Exception {
    Map<String, Object> params = new HashMap<>();
    params.put("emulatorId", emulatorId);
    params.put("fileName", fileName);
    return getRestClient().post(API + "directb2s/removeDmdImage", params, Boolean.class);
  }

  //--------------------------------
  // screen res management

  public DirectB2sScreenRes getGlobalScreenRes() {
    return getRestClient().get(API + "directb2s/screenRes", DirectB2sScreenRes.class);
  }

  public DirectB2sScreenRes getScreenRes(int emulatorId, String fileName, boolean tableOnly) {
    Map<String, Object> params = new HashMap<>();
    params.put("emulatorId", emulatorId);
    params.put("fileName", fileName);
    return getRestClient().post(API + "directb2s/screenRes" + (tableOnly ? "?tableOnly=true" : ""),
        params, DirectB2sScreenRes.class);
  }

  public InputStream getScreenResFrame(DirectB2sScreenRes screenres) throws IOException {
    String url = getRestClient().getBaseUrl() + API + "directb2s/frame/" + screenres.getEmulatorId() + "/"
        + URLEncoder.encode(URLEncoder.encode(screenres.getBackgroundFilePath(), StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    try {
      return new URL(url).openStream();
    }
    catch (FileNotFoundException e) {
      LOG.info("No .res file found for " + screenres.getB2SFileName());
    }
    return null;
  }

  public InputStream generateFrame(int emulatorId, String b2sFileName, DirectB2SFrameType frameType) {
    String url = getRestClient().getBaseUrl() + API + "directb2s/frame/" + emulatorId
        + "/" + URLEncoder.encode(URLEncoder.encode(b2sFileName, StandardCharsets.UTF_8), StandardCharsets.UTF_8)
        + "/" + frameType.name();
    try {
      return new URL(url).openStream();
    }
    catch (Exception e) {
      LOG.info("cannot get generated frame file for {}, emulator {}", b2sFileName, emulatorId);
    }
    return null;
  }


  public ReturnMessage saveScreenRes(DirectB2sScreenRes screenres) {
    return getRestClient().post(API + "directb2s/screenRes/save", screenres, ReturnMessage.class);
  }

  public String uploadScreenResFrame(int emulatorId, String filename, File file) {
    return uploadFile(emulatorId, filename,
        "directb2s/screenRes/uploadFrame", file, String.class);
  }

  public boolean removeScreenResFrame(int emulatorId, String filename) throws IOException {
    Map<String, Object> map = new HashMap<>();
    map.put("emuid", emulatorId);
    map.put("filename", filename);
    return getRestClient().delete(API + "directb2s/screenRes/removeFrame", map);
  }

  //--------------------------------
  // common methods

  private <T> T uploadFile(int emulatorId, String fileName, String suburl, File file, Class<T> responseType) {
    try {
      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add("emulatorId", emulatorId);
      map.add("fileName", fileName);
      String url = getRestClient().getBaseUrl() + API + suburl;
      HttpEntity<?> upload = createUpload(map, file, -1, null, AssetType.DIRECTB2S, null);

      //new RestTemplate().exchange(url, HttpMethod.POST, upload, Boolean.class);
      ResponseEntity<T> ret = createUploadTemplate().exchange(url, HttpMethod.POST, upload, responseType);
      finalizeUpload(upload);
      return ret.getBody();
    }
    catch (Exception e) {
      LOG.error("Upload failed: " + e.getMessage(), e);
      throw e;
    }
  }
}
