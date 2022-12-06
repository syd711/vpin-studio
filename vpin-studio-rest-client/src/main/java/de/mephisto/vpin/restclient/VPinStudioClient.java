package de.mephisto.vpin.restclient;

import de.mephisto.vpin.restclient.representations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VPinStudioClient implements ObservedPropertyChangeListener, OverlayClient {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public final static String API = "api/v1/";

  private final Map<String, ObservedProperties> observedProperties = new HashMap<>();

  private final Map<String, byte[]> imageCache = new HashMap<>();


  private Map<String, AssetRepresentation> assetCache = new HashMap<>();

  private VPinStudioClient() {

  }

  public static VPinStudioClient create() {
    return new VPinStudioClient();
  }

  /*********************************************************************************************************************
   * Assets / Popper
   ********************************************************************************************************************/

  public GameMediaRepresentation getGameMedia(int id) {
    return RestClient.getInstance().get(API + "poppermedia/" + id, GameMediaRepresentation.class);
  }

  public ByteArrayInputStream getGameMediaItem(int id, PopperScreen screen) {
    if (!imageCache.containsKey(String.valueOf(id)) && screen.equals(PopperScreen.Wheel)) {
      byte[] bytes = RestClient.getInstance().readBinary(API + "poppermedia/" + id + "/" + screen.name());
      if (bytes == null) {
        bytes = new byte[]{};
      }
      imageCache.put(String.valueOf(id), bytes);
    }

    if (screen.equals(PopperScreen.Wheel)) {
      byte[] imageBytes = imageCache.get(String.valueOf(id));
      return new ByteArrayInputStream(imageBytes);
    }

    byte[] bytes = RestClient.getInstance().readBinary(API + "poppermedia/" + id + "/" + screen.name());
    return new ByteArrayInputStream(bytes);
  }

  public AssetRepresentation uploadAsset(File file, long id, int maxSize, AssetType assetType) throws Exception {
    try {
      String url = RestClient.getInstance().getBaseUrl() + API + "assets/" + id + "/upload/" + maxSize;
      ResponseEntity<AssetRepresentation> exchange = new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, assetType), AssetRepresentation.class);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("Asset upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public ByteArrayInputStream getAsset(String uuid) {
    byte[] bytes = RestClient.getInstance().readBinary(API + "assets/data/" + uuid);
    if (bytes == null) {
      throw new UnsupportedOperationException("No data found for asset with UUID " + uuid);
    }
    return new ByteArrayInputStream(bytes);
  }

  /*********************************************************************************************************************
   * Preferences
   ********************************************************************************************************************/

  public PreferenceEntryRepresentation getPreference(String key) {
    return RestClient.getInstance().get(API + "preferences/" + key, PreferenceEntryRepresentation.class);
  }

  public boolean setPreferences(Map<String, Object> values) {
    try {
      return RestClient.getInstance().put(API + "preferences", values);
    } catch (Exception e) {
      LOG.error("Failed to set preferences: " + e.getMessage(), e);
    }
    return false;
  }

  public boolean setPreference(String key, Object value) {
    try {
      Map<String, Object> values = new HashMap<>();
      values.put(key, value);
      return setPreferences(values);
    } catch (Exception e) {
      LOG.error("Failed to set preference: " + e.getMessage(), e);
    }
    return false;
  }

  public boolean uploadVPinAvatar(File file) throws Exception {
    try {
      String url = RestClient.getInstance().getBaseUrl() + API + "preferences/avatar";
      new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, AssetType.VPIN_AVATAR), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Background upload failed: " + e.getMessage(), e);
      throw e;
    }
  }


  /*********************************************************************************************************************
   * Competitions
   ********************************************************************************************************************/

  public List<CompetitionRepresentation> getCompetitions() {
    return Arrays.asList(RestClient.getInstance().get(API + "competitions", CompetitionRepresentation[].class));
  }

  public List<CompetitionRepresentation> getFinishedCompetitions(int limit) {
    return Arrays.asList(RestClient.getInstance().get(API + "competitions/finished/" + limit, CompetitionRepresentation[].class));
  }


  public List<CompetitionRepresentation> getActiveOfflineCompetitions() {
    try {
      return Arrays.asList(RestClient.getInstance().get(API + "competitions/active/offline", CompetitionRepresentation[].class));
    } catch (Exception e) {
      LOG.error("Failed to read active competition: " + e.getMessage(), e);
    }
    return null;
  }

  public CompetitionRepresentation saveCompetition(CompetitionRepresentation c) throws Exception {
    try {
      return RestClient.getInstance().post(API + "competitions/save", c, CompetitionRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save competition: " + e.getMessage(), e);
      throw e;
    }
  }

  public void deleteCompetition(CompetitionRepresentation c) {
    try {
      RestClient.getInstance().delete(API + "competitions/delete/" + c.getId());
    } catch (Exception e) {
      LOG.error("Failed to delete competition: " + e.getMessage(), e);
    }
  }

  public ScoreListRepresentation getCompetitionScores(long id) {
    try {
      return RestClient.getInstance().get(API + "competitions/scores/" + id, ScoreListRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to read competition scores " + id + ": " + e.getMessage(), e);
    }
    return null;
  }

  public List<String> getCompetitionBadges() {
    return Arrays.asList(RestClient.getInstance().get(API + "competitions/badges", String[].class));
  }

  public ByteArrayInputStream getCompetitionBadge(String name) {
    if (!imageCache.containsKey(name)) {
      String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
      byte[] bytes = RestClient.getInstance().readBinary(API + "competitions/badge/" + encodedName);
      imageCache.put(name, bytes);
    }

    byte[] imageBytes = imageCache.get(name);
    return new ByteArrayInputStream(imageBytes);
  }

  public ByteArrayInputStream getCompetitionBackground(long gameId) {
    byte[] bytes = RestClient.getInstance().readBinary(API + "directb2s/competition/" + gameId);
    if (bytes == null) {
      throw new UnsupportedOperationException("No data found for competition with gameId " + gameId);
    }
    return new ByteArrayInputStream(bytes);
  }

  /*********************************************************************************************************************
   * Games
   ********************************************************************************************************************/

  public GameRepresentation getGame(int id) {
    try {
      return RestClient.getInstance().get(API + "games/" + id, GameRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to read game " + id + ": " + e.getMessage(), e);
    }
    return null;
  }

  public ScoreSummaryRepresentation getGameScores(int id) {
    try {
      return RestClient.getInstance().get(API + "games/scores/" + id, ScoreSummaryRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to read game scores " + id + ": " + e.getMessage(), e);
    }
    return null;
  }

  public boolean scanGame(GameRepresentation game) {
    int gameId = game.getId();
    return RestClient.getInstance().get(API + "games/scan/" + gameId, Boolean.class);
  }

  public GameRepresentation saveGame(GameRepresentation game) throws Exception {
    try {
      return RestClient.getInstance().post(API + "games/save", game, GameRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save game: " + e.getMessage(), e);
      throw e;
    }
  }

  public List<GameRepresentation> getGames() {
    return Arrays.asList(RestClient.getInstance().get(API + "games", GameRepresentation[].class));
  }

  public List<GameRepresentation> getRecentlyPlayedGames(int count) {
    return Arrays.asList(RestClient.getInstance().get(API + "/games/recent/" + count, GameRepresentation[].class));
  }

  /*********************************************************************************************************************
   * Player
   ********************************************************************************************************************/

  public PlayerRepresentation savePlayer(PlayerRepresentation p) throws Exception {
    try {
      return RestClient.getInstance().post(API + "players/save", p, PlayerRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save player: " + e.getMessage(), e);
      throw e;
    }
  }

  public void deletePlayer(PlayerRepresentation p) {
    try {
      RestClient.getInstance().delete(API + "players/delete/" + p.getId());
    } catch (Exception e) {
      LOG.error("Failed to delete player: " + e.getMessage(), e);
    }
  }

  public List<PlayerRepresentation> getPlayers() {
    return Arrays.asList(RestClient.getInstance().get(API + "players", PlayerRepresentation[].class));
  }

  public List<PlayerRepresentation> getPlayers(PlayerDomain domain) {
    return Arrays.asList(RestClient.getInstance().get(API + "players/domain/" + domain.name(), PlayerRepresentation[].class));
  }

  public ScoreSummaryRepresentation getPlayerScores(String initials) {
    return RestClient.getInstance().get(API + "players/highscores/" + initials, ScoreSummaryRepresentation.class);
  }

  public boolean invalidatePlayerDomain(PlayerDomain domain) {
    return RestClient.getInstance().get(API + "players/invalidate/" + domain.name(), Boolean.class);
  }


  /*********************************************************************************************************************
   * Overlay
   ********************************************************************************************************************/

  public InputStream getOverlayImage() {
    byte[] bytes = RestClient.getInstance().readBinary(API + "overlay/preview");
    return new ByteArrayInputStream(bytes);
  }

  public boolean generateOverlayImage() {
    return RestClient.getInstance().get(API + "overlay/generate", Boolean.class);
  }

  public List<String> getOverlayBackgrounds() {
    return Arrays.asList(RestClient.getInstance().get(API + "overlay/backgrounds", String[].class));
  }


  /*********************************************************************************************************************
   * Highscore Cards
   ********************************************************************************************************************/

  public ByteArrayInputStream getHighscoreCard(GameRepresentation game) {
    int gameId = game.getId();
    byte[] bytes = RestClient.getInstance().readBinary(API + "cards/preview/" + gameId);
    return new ByteArrayInputStream(bytes);
  }

  public boolean generateHighscoreCard(GameRepresentation game) {
    int gameId = game.getId();
    return RestClient.getInstance().get(API + "cards/generate/" + gameId, Boolean.class);
  }

  public boolean uploadOverlayBackgroundImage(File file) throws Exception {
    try {
      String url = RestClient.getInstance().getBaseUrl() + API + "overlay/backgroundupload";
      new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, AssetType.OVERLAY_BACKGROUND), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Background upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public List<String> getHighscoreBackgroundImages() {
    return Arrays.asList(RestClient.getInstance().get(API + "cards/backgrounds", String[].class));
  }

  public ByteArrayInputStream getOverlayBackgroundImage(String name) {
    if (!imageCache.containsKey(name)) {
      String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
      byte[] bytes = RestClient.getInstance().readBinary(API + "overlay/background/" + encodedName);
      imageCache.put(name, bytes);
    }

    byte[] imageBytes = imageCache.get(name);
    return new ByteArrayInputStream(imageBytes);
  }

  public ByteArrayInputStream getHighscoreBackgroundImage(String name) {
    if (!imageCache.containsKey(name)) {
      String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
      byte[] bytes = RestClient.getInstance().readBinary(API + "cards/background/" + encodedName);
      imageCache.put(name, bytes);
    }

    byte[] imageBytes = imageCache.get(name);
    return new ByteArrayInputStream(imageBytes);
  }

  public boolean uploadHighscoreBackgroundImage(File file) throws Exception {
    try {
      String url = RestClient.getInstance().getBaseUrl() + API + "cards/backgroundupload";
      new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, AssetType.CARD_BACKGROUND), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Background upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean uploadTable(File file) throws Exception {
    try {
      String url = RestClient.getInstance().getBaseUrl() + API + "games/upload/table";
      new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, AssetType.TABLE), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean uploadRom(File file) throws Exception {
    try {
      String url = RestClient.getInstance().getBaseUrl() + API + "games/upload/rom";
      new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, AssetType.ROM), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Rom upload failed: " + e.getMessage(), e);
      throw e;
    }
  }


  /*********************************************************************************************************************
   * Direct2B
   ********************************************************************************************************************/

  public ByteArrayInputStream getDirectB2SImage(GameRepresentation game) {
    byte[] bytes = RestClient.getInstance().readBinary(API + "directb2s/" + game.getId());
    return new ByteArrayInputStream(bytes);
  }

  public boolean uploadDirectB2SFile(File file, String uploadType, int gameId) throws Exception {
    try {
      String url = RestClient.getInstance().getBaseUrl() + API + "cards/directb2supload";
      new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, gameId, uploadType, AssetType.DIRECT_B2S), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Directb2s upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  /*********************************************************************************************************************
   * Utils
   */


  private static HttpEntity createUpload(File file, int gameId, String uploadType, AssetType assetType) throws Exception {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String boundary = Long.toHexString(System.currentTimeMillis());
    headers.set("Content-Type", "multipart/form-data; boundary=" + boundary);
    FileSystemResource rsr = new FileSystemResource(file);

    byte[] bFile = new byte[(int) file.length()];
    FileInputStream fileInputStream = new FileInputStream(file);
    fileInputStream.read(bFile);
    fileInputStream.close();


    map.add("file", rsr);
    map.add("gameId", gameId);
    map.add("uploadType", uploadType);
    map.add("assetType", assetType.name());
    return new HttpEntity<>(map, headers);
  }

  public ObservedProperties getProperties(String propertiesName) {
    if (!observedProperties.containsKey(propertiesName)) {
      Map<String, Object> result = RestClient.getInstance().get(API + "properties/" + propertiesName, Map.class);
      Properties properties = new Properties();
      properties.putAll(result);
      ObservedProperties observedProperties = new ObservedProperties(propertiesName, properties);
      observedProperties.setObserver(this);
      this.observedProperties.put(propertiesName, observedProperties);
    }

    return this.observedProperties.get(propertiesName);
  }

  public String getURL(String segment) {
    if (!segment.startsWith("http") && !segment.contains(API)) {
      return RestClient.getInstance().getBaseUrl() + API + segment;
    }
    return segment;
  }

  @Override
  public void changed(String propertiesName, String key, Optional<String> updatedValue) {
    try {
      Map<String, Object> model = new HashMap<>();
      model.put(key, updatedValue.get());
      Boolean result = RestClient.getInstance().put(API + "properties/" + propertiesName, model);
      ObservedProperties obsprops = this.observedProperties.get(propertiesName);
      obsprops.notifyChange(key, updatedValue.get());
    } catch (Exception e) {
      LOG.error("Failed to upload changed properties: " + e.getMessage(), e);
    }
  }
}
