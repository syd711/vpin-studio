package de.mephisto.vpin.restclient;

import de.mephisto.vpin.restclient.descriptors.*;
import de.mephisto.vpin.restclient.discord.DiscordBotStatus;
import de.mephisto.vpin.restclient.discord.DiscordChannel;
import de.mephisto.vpin.restclient.discord.DiscordCompetitionData;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.representations.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URL;
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
    this.restClient.clearCache();
    LOG.info("Cleared " + size + " resources from cache.");
  }

  public void clearWheelCache() {
    List<String> keys = new ArrayList<>(imageCache.keySet());
    for (String key : keys) {
      if (key.contains("/Wheel")) {
        imageCache.remove(key);
      }
    }
  }

  public void clearTableCache() {
    restClient.clearCache("games/");
  }

  public void clearDiscordCache() {
    restClient.clearCache("discord/");
  }

  /*********************************************************************************************************************
   * Misc
   ********************************************************************************************************************/
  public InputStream getCachedUrlImage(String imageUrl) {
    try {
      if (!imageCache.containsKey(imageUrl)) {
        URL url = new URL(imageUrl);
        ByteArrayOutputStream bis = new ByteArrayOutputStream();
        InputStream is = null;
        is = url.openStream();
        byte[] bytebuff = new byte[4096];
        int n;

        while ((n = is.read(bytebuff)) > 0) {
          bis.write(bytebuff, 0, n);
        }
        is.close();
        bis.close();

        byte[] bytes = bis.toByteArray();
        imageCache.put(imageUrl, bytes);
        LOG.info("Cached image URL " + imageUrl);
      }
    } catch (IOException e) {
      LOG.error("Failed to read image from URL: " + e.getMessage(), e);
    }

    byte[] bytes = imageCache.get(imageUrl);
    return new ByteArrayInputStream(bytes);
  }

  /*********************************************************************************************************************
   * Alt Sound
   ********************************************************************************************************************/
  public AltSound saveAltSound(int gameId, AltSound altSound) throws Exception {
    return restClient.post(API + "altsound/save/" + gameId, altSound, AltSound.class);
  }

  public AltSound getAltSound(int gameId) {
    return restClient.get(API + "altsound/" + gameId, AltSound.class);
  }

  public AltSound restoreAltSound(int gameId) {
    return restClient.get(API + "altsound/restore/" + gameId, AltSound.class);
  }


  /*********************************************************************************************************************
   * Table Manager
   ********************************************************************************************************************/
  public TableManagerSettings getTableManagerSettings() {
    return restClient.get(API + "popper/manager", TableManagerSettings.class);
  }

  public boolean saveTableManagerSettings(TableManagerSettings descriptor) throws Exception {
    return restClient.post(API + "popper/manager", descriptor, Boolean.class);
  }

  /*********************************************************************************************************************
   * Discord
   ********************************************************************************************************************/
  public DiscordCompetitionData getDiscordCompetitionData(long serverId, long channelId) {
    return restClient.get(API + "discord/competition/" + serverId + "/" + channelId, DiscordCompetitionData.class);
  }

  public DiscordBotStatus getDiscordStatus() {
    return restClient.get(API + "discord/status", DiscordBotStatus.class);
  }

  public List<DiscordChannel> getDiscordChannels() {
    return Arrays.asList(restClient.getCached(API + "discord/channels", DiscordChannel[].class));
  }

  public PlayerRepresentation getDiscordPlayer(long serverId, long memberId) {
    return restClient.getCached(API + "discord/player/" + serverId + "/" + memberId, PlayerRepresentation.class);
  }

  public List<DiscordChannel> getDiscordChannels(long serverId) {
    return Arrays.asList(restClient.getCached(API + "discord/channels/" + serverId, DiscordChannel[].class));
  }

  public DiscordServer getDiscordServer(long serverId) {
    return restClient.getCached(API + "discord/server/" + serverId, DiscordServer.class);
  }

  public List<DiscordServer> getDiscordServers() {
    return Arrays.asList(restClient.getCached(API + "discord/servers", DiscordServer[].class));
  }

  /*********************************************************************************************************************
   * Jobs
   ********************************************************************************************************************/
  public List<JobDescriptor> getJobs() {
    return Arrays.asList(restClient.get(API + "jobs", JobDescriptor[].class));
  }

  /*********************************************************************************************************************
   * IO
   ********************************************************************************************************************/
  public boolean backupTable(BackupDescriptor exportDescriptor) throws Exception {
    return restClient.post(API + "io/backup", exportDescriptor, Boolean.class);
  }

  public boolean installTable(ArchiveInstallDescriptor descriptor) throws Exception {
    return restClient.post(API + "io/install", descriptor, Boolean.class);
  }

  public boolean downloadArchive(ArchiveDownloadDescriptor descriptor) throws Exception {
    return restClient.post(API + "io/download", descriptor, Boolean.class);
  }

  /*********************************************************************************************************************
   * Archiving
   ********************************************************************************************************************/
  public List<ArchiveDescriptorRepresentation> getArchiveDescriptors(long id) {
    return Arrays.asList(restClient.get(API + "archives/" + id, ArchiveDescriptorRepresentation[].class));
  }

  public List<ArchiveDescriptorRepresentation> getArchiveDescriptorsFiltered() {
    return Arrays.asList(restClient.get(API + "archives/filtered", ArchiveDescriptorRepresentation[].class));
  }

  public List<ArchiveSourceRepresentation> getArchiveSources() {
    return Arrays.asList(restClient.get(API + "archives/sources", ArchiveSourceRepresentation[].class));
  }

  public void deleteArchive(long sourceId, String filename) {
    restClient.delete(API + "archives/descriptor/" + sourceId + "/" + filename);
  }

  public void deleteArchiveSource(long id) {
    restClient.delete(API + "archives/source/" + id);
  }

  public ArchiveSourceRepresentation saveArchiveSource(ArchiveSourceRepresentation source) throws Exception {
    try {
      return restClient.post(API + "archives/save", source, ArchiveSourceRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to save archive source: " + e.getMessage(), e);
      throw e;
    }
  }

  public List<ArchiveDescriptorRepresentation> getArchiveDescriptorsForGame(int gameId) {
    return Arrays.asList(restClient.get(API + "archives/game/" + gameId, ArchiveDescriptorRepresentation[].class));
  }

  public boolean invalidateArchiveCache(long sourceAdapterId) {
    return restClient.get(API + "archives/invalidate/" + sourceAdapterId, Boolean.class);
  }

  public String uploadArchive(File file, int repositoryId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = restClient.getBaseUrl() + API + "archives/upload/";
      HttpEntity upload = createUpload(file, repositoryId, null, AssetType.ARCHIVE, listener);
      return new RestTemplate().exchange(url, HttpMethod.POST, upload, String.class).getBody();
    } catch (Exception e) {
      LOG.error("Archive upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean downloadArchive(ArchiveDescriptorRepresentation descriptor) throws Exception {
    try {
      return restClient.post(API + "archives/install", descriptor, Boolean.class);
    } catch (Exception e) {
      LOG.error("Failed install archive: " + e.getMessage(), e);
      throw e;
    }
  }

  /*********************************************************************************************************************
   * Popper
   ********************************************************************************************************************/
  public PinUPControl getPinUPControlFor(PopperScreen screen) {
    return restClient.get(API + "popper/pincontrol/" + screen.name(), PinUPControl.class);
  }

  public PinUPControls getPinUPControls() {
    return restClient.get(API + "popper/pincontrols", PinUPControls.class);
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

  public boolean restartPopper() {
    return restClient.get(API + "popper/restart", Boolean.class);
  }

  public TableDetails getTableDetails(int gameId) {
    return restClient.get(API + "popper/tabledetails/" + gameId, TableDetails.class);
  }

  public TableDetails saveTableDetails(TableDetails tableDetails, int gameId) throws Exception {
    try {
      return restClient.post(API + "popper/tabledetails/" + gameId, tableDetails, TableDetails.class);
    } catch (Exception e) {
      LOG.error("Failed save table details: " + e.getMessage(), e);
      throw e;
    }
  }

  /*********************************************************************************************************************
   * VPX
   ********************************************************************************************************************/
  public void playGame(int id) {
    try {
      restClient.put(API + "vpx/play/" + id, new HashMap<>());
    } catch (Exception e) {
      LOG.error("Failed to start game " + id + ": " + e.getMessage(), e);
    }
  }


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
        LOG.info("Update POV property " + property + " to " + value);
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

  public boolean uploadDefaultBackgroundFile(File file, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = restClient.getBaseUrl() + API + "assets/background";
      new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, gameId, null, AssetType.DEFAULT_BACKGROUND, listener), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Default background upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean deleteDefaultBackgroundFile(int gameId) {
    try {
      restClient.delete(API + "assets/background/" + gameId);
      return true;
    } catch (Exception e) {
      LOG.error("Default background deletion failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public ByteArrayInputStream getGameMediaItem(int id, PopperScreen screen) {
    String url = API + "poppermedia/" + id + "/" + screen.name();
    if (!imageCache.containsKey(url) && screen.equals(PopperScreen.Wheel)) {
      byte[] bytes = restClient.readBinary(url);
      if (bytes == null) {
        bytes = new byte[]{};
      }
      imageCache.put(url, bytes);
    }

    if (screen.equals(PopperScreen.Wheel)) {
      byte[] imageBytes = imageCache.get(url);
      return new ByteArrayInputStream(imageBytes);
    }

    byte[] bytes = restClient.readBinary(url);
    return new ByteArrayInputStream(bytes);
  }

  public AssetRepresentation uploadAsset(File file, long id, int maxSize, AssetType assetType, FileUploadProgressListener listener) throws Exception {
    try {
      String url = restClient.getBaseUrl() + API + "assets/" + id + "/upload/" + maxSize;
      LOG.info("HTTP POST " + url);
      ResponseEntity<AssetRepresentation> exchange = new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, -1, null, assetType, listener), AssetRepresentation.class);
      return exchange.getBody();
    } catch (Exception e) {
      LOG.error("Asset upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public ByteArrayInputStream getAsset(AssetType assetType, String uuid) {
    if (assetType.equals(AssetType.AVATAR) && imageCache.containsKey(uuid)) {
      return new ByteArrayInputStream(imageCache.get(uuid));
    }

    byte[] bytes = restClient.readBinary(API + "assets/data/" + uuid);
    if (bytes == null) {
      throw new UnsupportedOperationException("No data found for asset with UUID " + uuid);
    }

    if (assetType.equals(AssetType.AVATAR)) {
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

  public boolean hasManagePermissions(long serverId, long channelId) {
    return restClient.get(API + "discord/permissions/competitions/manage/" + serverId + "/" + channelId, Boolean.class);
  }

  public boolean hasJoinPermissions(long serverId, long channelId) {
    return restClient.get(API + "discord/permissions/competitions/join/" + serverId + "/" + channelId, Boolean.class);
  }

  public CompetitionRepresentation getCompetitionByUuid(String uuid) {
    return restClient.get(API + "competitions/competition/" + uuid, CompetitionRepresentation.class);
  }

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


  public CompetitionRepresentation getActiveCompetition(CompetitionType type) {
    try {
      return restClient.get(API + "competitions/" + type.name() + "/active", CompetitionRepresentation.class);
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

  public ScoreListRepresentation getCompetitionScoreList(long competitionId) {
    try {
      return restClient.get(API + "competitions/scores/" + competitionId, ScoreListRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to read competition scores list " + competitionId + ": " + e.getMessage(), e);
    }
    return null;
  }

  public ScoreSummaryRepresentation getCompetitionScore(long id) {
    try {
      return restClient.get(API + "competitions/score/" + id, ScoreSummaryRepresentation.class);
    } catch (Exception e) {
      LOG.error("Failed to read competition scores " + id + ": " + e.getMessage(), e);
    }
    return null;
  }

  public ByteArrayInputStream getCompetitionBackground(long gameId) {
    String name = "competition-bg-game-" + gameId;

    if (!imageCache.containsKey(name)) {
      byte[] bytes = restClient.readBinary(API + "assets/competition/" + gameId);
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

  public List<GameRepresentation> getGamesByRom(String rom) {
    try {
      return Arrays.asList(restClient.get(API + "games/rom/" + rom, GameRepresentation[].class));
    } catch (Exception e) {
      LOG.error("Failed to read games for " + rom + ": " + e.getMessage());
    }
    return null;
  }


  public GameRepresentation getGame(int id) {
    try {
      return restClient.getCached(API + "games/" + id, GameRepresentation.class);
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

  public boolean resetHighscore(ResetHighscoreDescriptor descriptor) throws Exception {
    return restClient.post(API + "games/reset", descriptor, Boolean.class);
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

  public PlayerRepresentation getPlayer(long serverId, String initials) {
    try {
      return restClient.get(API + "players/player/" + serverId + "/" + initials, PlayerRepresentation.class);
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

  public boolean uploadTable(File file, TableUploadDescriptor tableUploadDescriptor, int gameId, FileUploadProgressListener listener) {
    try {
      String url = restClient.getBaseUrl() + API + "games/upload/table";
      LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
      map.add("mode", tableUploadDescriptor.name());
      map.add("gameId", gameId);
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
   * DirectB2S
   ********************************************************************************************************************/

  public ByteArrayInputStream getDefaultPicture(GameRepresentation game) {
    byte[] bytes = restClient.readBinary(API + "assets/defaultbackground/" + game.getId());
    return new ByteArrayInputStream(bytes);
  }

  public DirectB2SData getDirectB2SData(int gameId) {
    return restClient.get(API + "directb2s/" + gameId, DirectB2SData.class);
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
  public void download(@NonNull String url, @NonNull File target) throws Exception {
    RestTemplate template = new RestTemplate();
    LOG.info("HTTP Download " + restClient.getBaseUrl() + API + url);
    File file = template.execute(restClient.getBaseUrl() + API + url, HttpMethod.GET, null, clientHttpResponse -> {
      FileOutputStream out = null;
      try {
        out = new FileOutputStream(target);
        StreamUtils.copy(clientHttpResponse.getBody(), out);
        return target;
      } catch (Exception e) {
        throw e;
      } finally {
        out.close();
      }
    });
  }

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
    map.add("objectId", gameId);
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
