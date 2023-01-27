package de.mephisto.vpin.restclient;

import de.mephisto.vpin.restclient.representations.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class VPinStudioClient implements ObservedPropertyChangeListener, OverlayClient {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  public final static String API = "api/v1/";

  private final Map<String, ObservedProperties> observedProperties = new HashMap<>();

  private final Map<String, byte[]> imageCache = new HashMap<>();

  private final RestClient restClient;

  private final String host;

  public VPinStudioClient(String host) {
    this.host = host;
    restClient = RestClient.createInstance(host);
  }

  public String getHost() {
    return host;
  }

  public void clearCache() {
    int size = this.imageCache.size();
    this.imageCache.clear();
    LOG.info("Cleared " + size + " resources from cache.");
  }

  /*********************************************************************************************************************
   * Discord
   ********************************************************************************************************************/
  public boolean isDiscordBotAvailable() {
    return restClient.get(API + "discord/available", Boolean.class);
  }

  public String getActiveCompetitionName(long channelId) {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(restClient.getBaseUrl() + API + "discord/channel/" + channelId + "/name", String.class);
  }

  public String getBotId() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(restClient.getBaseUrl() + API + "discord/botId", String.class);
  }

  public List<DiscordChannel> getDiscordChannels() {
    return Arrays.asList(restClient.get(API + "discord/channels", DiscordChannel[].class));
  }
  public PlayerRepresentation getDiscordPlayer(long serverId, long memberId) {
    return restClient.get(API + "discord/player/" + serverId + "/" + memberId, PlayerRepresentation.class);
  }

  public List<DiscordChannel> getDiscordChannels(long serverId) {
    return Arrays.asList(restClient.get(API + "discord/channels/" + serverId, DiscordChannel[].class));
  }

  public DiscordServer getDiscordServer(long serverId) {
    return restClient.get(API + "discord/server/" + serverId, DiscordServer.class);
  }

  public List<DiscordServer> getDiscordServers() {
    return Arrays.asList(restClient.get(API + "discord/servers", DiscordServer[].class));
  }

  /*********************************************************************************************************************
   * Jobs
   ********************************************************************************************************************/
  public List<JobDescriptor> getJobs() {
    return Arrays.asList(restClient.get(API + "jobs", JobDescriptor[].class));
  }

  /*********************************************************************************************************************
   * VPA
   ********************************************************************************************************************/
  public VpaManifest getVpaManifest(int gameId) {
    return restClient.get(API + "vpa/manifest/" + gameId, VpaManifest.class);
  }

  public boolean exportVpa(ExportDescriptor exportDescriptor) throws Exception {
    return restClient.post(API + "vpa/export", exportDescriptor, Boolean.class);
  }

  public String uploadVpa(File file, FileUploadProgressListener listener) throws Exception {
    try {
      String url = restClient.getBaseUrl() + API + "vpa/upload";
      return new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, AssetType.VPA, listener), String.class).getBody();
    } catch (Exception e) {
      LOG.error("VPA upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean importVpa(ImportDescriptor exportDescriptor) throws Exception {
    return restClient.post(API + "vpa/import", exportDescriptor, Boolean.class);
  }

  /*********************************************************************************************************************
   * Popper
   ********************************************************************************************************************/
  public PinUPControl getPinUPControlFor(PopperScreen screen) {
    return restClient.get(API + "popper/pincontrol/" + screen.name(), PinUPControl.class);
  }

  public List<PlaylistRepresentation> getPlaylists() {
    return Arrays.asList(restClient.get(API + "popper/playlists", PlaylistRepresentation[].class));
  }

  public boolean isPinUPPopperRunning() {
    return restClient.get(API + "popper/running", Boolean.class);
  }

  public boolean terminatePopper() {
    return restClient.get(API + "popper/terminate", Boolean.class);
  }


  /*********************************************************************************************************************
   * VPX
   ********************************************************************************************************************/
  public POVRepresentation getPOV(int gameId) {
    Map<String, Object> povData = restClient.get(API + "vpx/pov/" + gameId, Map.class);
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
        return restClient.put(API + "vpx/pov/" + gameId, values);
      }
      return true;
    } catch (Exception e) {
      LOG.error("Failed to set preferences: " + e.getMessage(), e);
    }
    return false;
  }


  public POVRepresentation createPOV(int gameId) throws Exception {
    try {
      return restClient.post(API + "vpx/pov/" + gameId, new HashMap<>(), POVRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to create POV representation: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean deletePOV(int gameId) {
    return restClient.delete(API + "vpx/pov/" + gameId);
  }

  public File getTableScript(GameRepresentation game) {
    final RestTemplate restTemplate = new RestTemplate();
    String src = restTemplate.getForObject(restClient.getBaseUrl() + API + "vpx/script/" + game.getId(), String.class);
    if (!StringUtils.isEmpty(src)) {
      try {
        File tmp = File.createTempFile(game.getGameDisplayName() + "-script-src", ".txt");

        Path path = Paths.get(tmp.toURI());
        byte[] strToBytes = src.getBytes();
        Files.write(path, strToBytes);

        tmp.deleteOnExit();
        return tmp;
      } catch (IOException e) {
        LOG.error("Failed to create temp file for script: " + e.getMessage(), e);
      }
    }
    return null;
  }

  /*********************************************************************************************************************
   * System
   ********************************************************************************************************************/

  public Date getStartupTime() {
    return restClient.get(API + "system/startupTime", Date.class);
  }

  public String logs() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(restClient.getBaseUrl() + API + "system/logs", String.class);
  }

  public void shutdown() {
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getForObject(restClient.getBaseUrl() + API + "system/shutdown", Boolean.class);
  }

  public boolean autostartInstalled() {
    final RestTemplate restTemplate = new RestTemplate();
    return Boolean.TRUE.equals(restTemplate.getForObject(restClient.getBaseUrl() + API + "system/autostart/installed", Boolean.class));
  }

  public boolean autostartInstall() {
    final RestTemplate restTemplate = new RestTemplate();
    return Boolean.TRUE.equals(restTemplate.getForObject(restClient.getBaseUrl() + API + "system/autostart/install", Boolean.class));
  }

  public boolean autostartUninstall() {
    final RestTemplate restTemplate = new RestTemplate();
    return Boolean.TRUE.equals(restTemplate.getForObject(restClient.getBaseUrl() + API + "system/autostart/uninstall", Boolean.class));
  }

  public void startServerUpdate(String version) {
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.getForObject(restClient.getBaseUrl() + API + "system/update/" + version + "/download/start", Boolean.class);
  }

  public int getServerUpdateProgress() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(restClient.getBaseUrl() + API + "system/update/download/status", Integer.class);
  }

  public boolean installServerUpdate() {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(restClient.getBaseUrl() + API + "system/update/install", Boolean.class);
  }

  public String version() {
    try {
      final RestTemplate restTemplate = new RestTemplate();
      return restTemplate.getForObject(restClient.getBaseUrl() + API + "system/version", String.class);
    } catch (Exception e) {
      LOG.error("Get version failed for " + restClient.getBaseUrl());
    }
    return null;
  }

  /*********************************************************************************************************************
   * Assets / Popper
   ********************************************************************************************************************/

  public ByteArrayInputStream getGameMediaItem(int id, PopperScreen screen) {
    if (!imageCache.containsKey(String.valueOf(id)) && screen.equals(PopperScreen.Wheel)) {
      byte[] bytes = restClient.readBinary(API + "poppermedia/" + id + "/" + screen.name());
      if (bytes == null) {
        bytes = new byte[]{};
      }
      imageCache.put(String.valueOf(id), bytes);
    }

    if (screen.equals(PopperScreen.Wheel)) {
      byte[] imageBytes = imageCache.get(String.valueOf(id));
      return new ByteArrayInputStream(imageBytes);
    }

    byte[] bytes = restClient.readBinary(API + "poppermedia/" + id + "/" + screen.name());
    return new ByteArrayInputStream(bytes);
  }

  public AssetRepresentation uploadAsset(File file, long id, int maxSize, AssetType assetType, FileUploadProgressListener listener) throws Exception {
    try {
      String url = restClient.getBaseUrl() + API + "assets/" + id + "/upload/" + maxSize;
      ResponseEntity<AssetRepresentation> exchange = new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, assetType, listener), AssetRepresentation.class);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("Asset upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public ByteArrayInputStream getAsset(AssetType assetType, String uuid) {
    if(assetType.equals(AssetType.AVATAR) && imageCache.containsKey(uuid)) {
      return new ByteArrayInputStream(imageCache.get(uuid));
    }

    byte[] bytes = restClient.readBinary(API + "assets/data/" + uuid);
    if (bytes == null) {
      throw new UnsupportedOperationException("No data found for asset with UUID " + uuid);
    }

    if(assetType.equals(AssetType.AVATAR)) {
      imageCache.put(uuid, bytes);
    }
    return new ByteArrayInputStream(bytes);
  }

  /*********************************************************************************************************************
   * Preferences
   ********************************************************************************************************************/

  public PreferenceEntryRepresentation getPreference(String key) {
    return restClient.get(API + "preferences/" + key, PreferenceEntryRepresentation.class);
  }

  public boolean setPreferences(Map<String, Object> values) {
    try {
      return restClient.put(API + "preferences", values);
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
      String url = restClient.getBaseUrl() + API + "preferences/avatar";
      new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, AssetType.VPIN_AVATAR, null), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Background upload failed: " + e.getMessage(), e);
      throw e;
    }
  }


  /*********************************************************************************************************************
   * Competitions
   ********************************************************************************************************************/

  public List<CompetitionRepresentation> getOfflineCompetitions() {
    return Arrays.asList(restClient.get(API + "competitions/offline", CompetitionRepresentation[].class));
  }

  public List<PlayerRepresentation> getDiscordCompetitionPlayers(long competitionId) {
    return Arrays.asList(restClient.get(API + "competitions/players/" + competitionId, PlayerRepresentation[].class));
  }

  public List<CompetitionRepresentation> getDiscordCompetitions() {
    return Arrays.asList(restClient.get(API + "competitions/discord", CompetitionRepresentation[].class));
  }

  public List<CompetitionRepresentation> getFinishedCompetitions(int limit) {
    return Arrays.asList(restClient.get(API + "competitions/finished/" + limit, CompetitionRepresentation[].class));
  }


  public List<CompetitionRepresentation> getActiveCompetitions() {
    try {
      return Arrays.asList(restClient.get(API + "competitions/active", CompetitionRepresentation[].class));
    } catch (Exception e) {
      LOG.error("Failed to read active competition: " + e.getMessage(), e);
    }
    return null;
  }

  public CompetitionRepresentation saveCompetition(CompetitionRepresentation c) throws Exception {
    try {
      return restClient.post(API + "competitions/save", c, CompetitionRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save competition: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean isGameReferencedByCompetitions(int gameId) {
    CompetitionRepresentation[] competitionRepresentations = restClient.get(API + "competitions/game/" + gameId, CompetitionRepresentation[].class);
    return competitionRepresentations.length > 0;
  }

  public void deleteCompetition(CompetitionRepresentation c) {
    try {
      restClient.delete(API + "competitions/" + c.getId());
    } catch (Exception e) {
      LOG.error("Failed to delete competition: " + e.getMessage(), e);
    }
  }

  public void finishCompetition(CompetitionRepresentation c) {
    try {
      restClient.put(API + "competitions/finish/" + c.getId(), Collections.emptyMap());
    } catch (Exception e) {
      LOG.error("Failed to finish competition: " + e.getMessage(), e);
    }
  }

  public ScoreListRepresentation getCompetitionScores(long id) {
    try {
      return restClient.get(API + "competitions/scores/" + id, ScoreListRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to read competition scores " + id + ": " + e.getMessage(), e);
    }
    return null;
  }

  public ByteArrayInputStream getCompetitionBackground(long gameId) {
    String name = "competition-bg-game-" + gameId;

    if (!imageCache.containsKey(name)) {
      byte[] bytes = restClient.readBinary(API + "directb2s/competition/" + gameId);
      imageCache.put(name, bytes);
    }

    byte[] imageBytes = imageCache.get(name);
    return new ByteArrayInputStream(imageBytes);
  }


  public List<String> getCompetitionBadges() {
    return Arrays.asList(restClient.get(API + "system/badges", String[].class));
  }

  public ByteArrayInputStream getCompetitionBadge(String name) {
    if (!imageCache.containsKey(name)) {
      String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
      byte[] bytes = restClient.readBinary(API + "system/badge/" + encodedName);
      imageCache.put(name, bytes);
    }

    byte[] imageBytes = imageCache.get(name);
    return new ByteArrayInputStream(imageBytes);
  }

  /*********************************************************************************************************************
   * Games
   ********************************************************************************************************************/
  public void deleteGame(@NonNull DeleteDescriptor descriptor) {
    try {
      restClient.post(API + "games/delete", descriptor, Boolean.class);
    } catch (Exception e) {
      LOG.error("Failed to delete game " + descriptor.getGameId() + ": " + e.getMessage(), e);
    }
  }

  public GameRepresentation getGame(int id) {
    try {
      return restClient.get(API + "games/" + id, GameRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to read game " + id + ": " + e.getMessage());
    }
    return null;
  }

  public int getGameCount() {
    try {
      final RestTemplate restTemplate = new RestTemplate();
      return restTemplate.getForObject(restClient.getBaseUrl() + API + "games/count", Integer.class);
    } catch (Exception e) {
      LOG.error("Failed to read game count: " + e.getMessage(), e);
    }
    return 0;
  }

  public List<Integer> getGameIds() {
    try {
      final RestTemplate restTemplate = new RestTemplate();
      return Arrays.asList(restTemplate.getForObject(restClient.getBaseUrl() + API + "games/ids", Integer[].class));
    } catch (Exception e) {
      LOG.error("Failed to read game ids: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  public ScoreSummaryRepresentation getGameScores(int id) {
    try {
      return restClient.get(API + "games/scores/" + id, ScoreSummaryRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to read game scores " + id + ": " + e.getMessage(), e);
    }
    return null;
  }

  public ScoreListRepresentation getScoreHistory(int gameId) {
    return restClient.get(API + "games/scorehistory/" + gameId, ScoreListRepresentation.class);
  }

  public HighscoreMetadataRepresentation scanGameScore(int id) {
    try {
      return restClient.get(API + "games/scanscore/" + id, HighscoreMetadataRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to read game scores " + id + ": " + e.getMessage(), e);
    }
    return null;
  }

  public GameRepresentation scanGame(int gameId) {
    return restClient.get(API + "games/scan/" + gameId, GameRepresentation.class);
  }

  public GameRepresentation saveGame(GameRepresentation game) throws Exception {
    try {
      return restClient.post(API + "games/save", game, GameRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save game: " + e.getMessage(), e);
      throw e;
    }
  }

  public List<GameRepresentation> getGames() {
    try {
      return Arrays.asList(restClient.get(API + "games", GameRepresentation[].class));
    } catch (Exception e) {
      LOG.error("Failed to save game: " + e.getMessage(), e);
      throw e;
    }
  }

  public List<GameRepresentation> getGamesWithScores() {
    return Arrays.asList(restClient.get(API + "games/scoredgames", GameRepresentation[].class));
  }

  public ScoreSummaryRepresentation getRecentlyPlayedGames(int count) {
    return restClient.get(API + "games/recent/" + count, ScoreSummaryRepresentation.class);
  }

  /*********************************************************************************************************************
   * Player
   ********************************************************************************************************************/

  public PlayerRepresentation savePlayer(PlayerRepresentation p) throws Exception {
    try {
      return restClient.post(API + "players/save", p, PlayerRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save player: " + e.getMessage(), e);
      throw e;
    }
  }

  public void deletePlayer(PlayerRepresentation p) {
    try {
      restClient.delete(API + "players/" + p.getId());
    } catch (Exception e) {
      LOG.error("Failed to delete player: " + e.getMessage(), e);
    }
  }

  public List<PlayerRepresentation> getPlayers() {
    return Arrays.asList(restClient.get(API + "players", PlayerRepresentation[].class));
  }

  public List<PlayerRepresentation> getPlayers(PlayerDomain domain) {
    return Arrays.asList(restClient.get(API + "players/domain/" + domain.name(), PlayerRepresentation[].class));
  }

  public PlayerRepresentation getPlayer(String initials) {
    try {
      return restClient.get(API + "players/initials/" + initials, PlayerRepresentation.class);
    } catch (Exception e) {
      //ignore
    }
    return null;
  }

  public List<RankedPlayerRepresentation> getRankedPlayers() {
    return Arrays.asList(restClient.get(API + "players/ranked", RankedPlayerRepresentation[].class));
  }

  public ScoreSummaryRepresentation getPlayerScores(String initials) {
    return restClient.get(API + "players/highscores/" + initials, ScoreSummaryRepresentation.class);
  }

  public boolean invalidatePlayerDomain(PlayerDomain domain) {
    return restClient.get(API + "players/invalidate/" + domain.name(), Boolean.class);
  }

  /*********************************************************************************************************************
   * Highscore Cards
   ********************************************************************************************************************/

  public ByteArrayInputStream getHighscoreCard(GameRepresentation game) {
    int gameId = game.getId();
    byte[] bytes = restClient.readBinary(API + "cards/preview/" + gameId);
    return new ByteArrayInputStream(bytes);
  }

  public boolean generateHighscoreCard(GameRepresentation game) {
    int gameId = game.getId();
    return restClient.get(API + "cards/generate/" + gameId, Boolean.class);
  }

  public boolean generateHighscoreCardSample(GameRepresentation game) {
    int gameId = game.getId();
    return restClient.get(API + "cards/generatesample/" + gameId, Boolean.class);
  }

  public List<String> getHighscoreBackgroundImages() {
    return Arrays.asList(restClient.get(API + "cards/backgrounds", String[].class));
  }

  public ByteArrayInputStream getOverlayBackgroundImage(String name) {
    if (!imageCache.containsKey(name)) {
      String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
      byte[] bytes = restClient.readBinary(API + "overlay/background/" + encodedName);
      imageCache.put(name, bytes);
    }

    byte[] imageBytes = imageCache.get(name);
    return new ByteArrayInputStream(imageBytes);
  }

  public ByteArrayInputStream getHighscoreBackgroundImage(String name) {
    if (!imageCache.containsKey(name)) {
      String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
      byte[] bytes = restClient.readBinary(API + "cards/background/" + encodedName);
      imageCache.put(name, bytes);
    }

    byte[] imageBytes = imageCache.get(name);
    return new ByteArrayInputStream(imageBytes);
  }

  public boolean uploadHighscoreBackgroundImage(File file, FileUploadProgressListener listener) throws Exception {
    try {
      String url = restClient.getBaseUrl() + API + "cards/backgroundupload";
      new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, AssetType.CARD_BACKGROUND, listener), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Background upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean uploadTable(File file, boolean importToPopper, int playlistId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = restClient.getBaseUrl() + API + "games/upload/table";
      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add("importToPopper", importToPopper);
      map.add("playlistId", playlistId);
      new RestTemplate().exchange(url, HttpMethod.POST, createUpload(map, file, -1, null, AssetType.TABLE, listener), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean uploadRom(File file, FileUploadProgressListener listener) throws Exception {
    try {
      String url = restClient.getBaseUrl() + API + "games/upload/rom";
      return Boolean.TRUE.equals(new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, AssetType.ROM, listener), Boolean.class).getBody());
    } catch (Exception e) {
      LOG.error("Rom upload failed: " + e.getMessage(), e);
      throw e;
    }
  }


  /*********************************************************************************************************************
   * Direct2B
   ********************************************************************************************************************/

  public ByteArrayInputStream getDirectB2SImage(GameRepresentation game) {
    byte[] bytes = restClient.readBinary(API + "directb2s/" + game.getId());
    return new ByteArrayInputStream(bytes);
  }

  public boolean uploadDirectB2SFile(File file, String uploadType, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = restClient.getBaseUrl() + API + "directb2s/upload";
      new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, gameId, uploadType, AssetType.DIRECT_B2S, listener), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Directb2s upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  /*********************************************************************************************************************
   * Utils
   */
  private static HttpEntity createUpload(File file, int gameId, String uploadType, AssetType assetType, FileUploadProgressListener listener) throws Exception {
    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
    return createUpload(map, file, gameId, uploadType, assetType, listener);
  }

  private static HttpEntity createUpload(LinkedMultiValueMap<String, Object> map, File file, int gameId, String uploadType, AssetType assetType, FileUploadProgressListener listener) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    String boundary = Long.toHexString(System.currentTimeMillis());
    headers.set("Content-Type", "multipart/form-data; boundary=" + boundary);
    ProgressableFileSystemResource rsr = new ProgressableFileSystemResource(file, listener);


    map.add("file", rsr);
    map.add("gameId", gameId);
    map.add("uploadType", uploadType);
    map.add("assetType", assetType.name());
    return new HttpEntity<>(map, headers);
  }

  public ObservedProperties getProperties(String propertiesName) {
    if (!observedProperties.containsKey(propertiesName)) {
      Map<String, Object> result = restClient.get(API + "properties/" + propertiesName, Map.class);
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
      return restClient.getBaseUrl() + API + segment;
    }
    return segment;
  }

  @Override
  public void changed(String propertiesName, String key, Optional<String> updatedValue) {
    try {
      Map<String, Object> model = new HashMap<>();
      model.put(key, updatedValue.get());
      Boolean result = restClient.put(API + "properties/" + propertiesName, model);
      ObservedProperties obsprops = this.observedProperties.get(propertiesName);
      obsprops.notifyChange(key, updatedValue.get());
    } catch (Exception e) {
      LOG.error("Failed to upload changed properties: " + e.getMessage(), e);
    }
  }
}
