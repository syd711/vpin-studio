package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.OverlayClient;
import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.highscores.ScoreListRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.players.RankedPlayerRepresentation;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.tables.GameRepresentation;
import de.mephisto.vpin.restclient.util.properties.ObservedProperties;
import de.mephisto.vpin.restclient.util.properties.ObservedPropertyChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

public class VPinStudioClient implements ObservedPropertyChangeListener, OverlayClient {
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);
  public final static String API = "api/v1/";

  private final Map<String, ObservedProperties> observedProperties = new HashMap<>();

  private final RestClient restClient;

  private final String host;

  private final AltSoundServiceClient altSoundServiceClient;
  private final AltColorServiceClient altColorServiceClient;
  private final ArchiveServiceClient archiveServiceClient;
  private final AssetServiceClient assetServiceClient;
  private final CompetitionsServiceClient competitions;
  private final BackglassServiceClient backglassServiceClient;
  private final DiscordServiceClient discordServiceClient;
  private final GamesServiceClient gamesServiceClient;
  private final HighscoreCardsServiceClient highscoreCardsServiceClient;
  private final ImageCache imageCache;
  private final JobsServiceClient jobsServiceClient;
  private final PlayersServiceClient playersServiceClient;
  private final PinUPPopperServiceClient pinUPPopperServiceClient;
  private final PreferencesServiceClient preferencesServiceClient;
  private final PupPackServiceClient pupPackServiceClient;
  private final SystemServiceClient systemServiceClient;
  private final MameServiceClient mameServiceClient;
  private final NVRamsServiceClient nvRamsServiceClient;
  private final TableManagerServiceClient tableManagerServiceClient;
  private final VpxServiceClient vpxServiceClient;
  private final VpbmServiceClient vpbmServiceClient;
  private final RomServiceClient romServiceClient;
  private final PinVolServiceClient pinVolServiceClient;
  private final PINemHiServiceClient pinemHiServiceClient;
  private final PlaylistsServiceClient playlistsServiceClient;
  private final HigscoreBackupServiceClient higscoreBackupServiceClient;

  public VPinStudioClient(String host) {
    this.host = host;
    restClient = RestClient.createInstance(host);

    this.altColorServiceClient = new AltColorServiceClient(this);
    this.altSoundServiceClient = new AltSoundServiceClient(this);
    this.archiveServiceClient = new ArchiveServiceClient(this);
    this.assetServiceClient = new AssetServiceClient(this);
    this.competitions = new CompetitionsServiceClient(this);
    this.backglassServiceClient = new BackglassServiceClient(this);
    this.discordServiceClient = new DiscordServiceClient(this);
    this.gamesServiceClient = new GamesServiceClient(this);
    this.highscoreCardsServiceClient = new HighscoreCardsServiceClient(this);
    this.imageCache = new ImageCache(this);
    this.jobsServiceClient = new JobsServiceClient(this);
    this.mameServiceClient = new MameServiceClient(this);
    this.nvRamsServiceClient = new NVRamsServiceClient(this);
    this.playersServiceClient = new PlayersServiceClient(this);
    this.pupPackServiceClient = new PupPackServiceClient(this);
    this.pinUPPopperServiceClient = new PinUPPopperServiceClient(this);
    this.preferencesServiceClient = new PreferencesServiceClient(this);
    this.systemServiceClient = new SystemServiceClient(this);
    this.tableManagerServiceClient = new TableManagerServiceClient(this);
    this.vpxServiceClient = new VpxServiceClient(this);
    this.vpbmServiceClient = new VpbmServiceClient(this);
    this.romServiceClient = new RomServiceClient(this);
    this.pinVolServiceClient = new PinVolServiceClient(this);
    this.pinemHiServiceClient = new PINemHiServiceClient(this);
    this.playlistsServiceClient = new PlaylistsServiceClient(this);
    this.higscoreBackupServiceClient = new HigscoreBackupServiceClient(this);
  }

  public NVRamsServiceClient getNvRamsService() {
    return nvRamsServiceClient;
  }

  public HigscoreBackupServiceClient getHigscoreBackupService() {
    return higscoreBackupServiceClient;
  }

  public PlaylistsServiceClient getPlaylistsService() {
    return playlistsServiceClient;
  }

  public PINemHiServiceClient getPINemHiService() {
    return pinemHiServiceClient;
  }

  public PinVolServiceClient getPinVolService() {
    return pinVolServiceClient;
  }

  public RomServiceClient getRomService() {
    return romServiceClient;
  }

  public AltColorServiceClient getAltColorService() {
    return altColorServiceClient;
  }

  public MameServiceClient getMameService() {
    return mameServiceClient;
  }

  public PupPackServiceClient getPupPackService() {
    return pupPackServiceClient;
  }

  public AltSoundServiceClient getAltSoundService() {
    return altSoundServiceClient;
  }

  public ArchiveServiceClient getArchiveService() {
    return archiveServiceClient;
  }

  public AssetServiceClient getAssetService() {
    return assetServiceClient;
  }

  public CompetitionsServiceClient getCompetitionService() {
    return competitions;
  }

  public BackglassServiceClient getBackglassServiceClient() {
    return backglassServiceClient;
  }

  public DiscordServiceClient getDiscordService() {
    return discordServiceClient;
  }

  public GamesServiceClient getGameService() {
    return gamesServiceClient;
  }

  public HighscoreCardsServiceClient getHighscoreCardsService() {
    return highscoreCardsServiceClient;
  }

  public ImageCache getImageCache() {
    return imageCache;
  }

  public JobsServiceClient getJobsService() {
    return jobsServiceClient;
  }

  public PlayersServiceClient getPlayerService() {
    return playersServiceClient;
  }

  public PinUPPopperServiceClient getPinUPPopperService() {
    return pinUPPopperServiceClient;
  }

  public PreferencesServiceClient getPreferenceService() {
    return preferencesServiceClient;
  }

  public SystemServiceClient getSystemService() {
    return systemServiceClient;
  }

  public TableManagerServiceClient getTableManagerService() {
    return tableManagerServiceClient;
  }

  public VpxServiceClient getVpxService() {
    return vpxServiceClient;
  }

  public VpbmServiceClient getVpbmService() {
    return vpbmServiceClient;
  }

  @Override
  public DiscordServer getDiscordServer(long serverId) {
    return discordServiceClient.getDiscordServer(serverId);
  }

  @Override
  public List<CompetitionRepresentation> getFinishedCompetitions(int limit) {
    return getCompetitionService().getFinishedCompetitions(limit);
  }

  @Override
  public CompetitionRepresentation getActiveCompetition(CompetitionType type) {
    return getCompetitionService().getActiveCompetition(type);
  }

  @Override
  public GameRepresentation getGame(int id) {
    return getGameService().getGame(id);
  }

  @Override
  public InputStream getCachedUrlImage(String url) {
    return getImageCache().getCachedUrlImage(url);
  }

  @Override
  public ScoreSummaryRepresentation getCompetitionScore(long id) {
    return getCompetitionService().getCompetitionScore(id);
  }

  @Override
  public ByteArrayInputStream getCompetitionBackground(long gameId) {
    return getCompetitionService().getCompetitionBackground(gameId);
  }

  @Override
  public ScoreListRepresentation getCompetitionScoreList(long id) {
    return getCompetitionService().getCompetitionScoreList(id);
  }

  @Override
  public ByteArrayInputStream getAsset(AssetType assetType, String uuid) {
    return getAssetService().getAsset(assetType, uuid);
  }

  @Override
  public ScoreSummaryRepresentation getRecentlyPlayedGames(int count) {
    return getGameService().getRecentlyPlayedGames(count);
  }

  @Override
  public ByteArrayInputStream getGameMediaItem(int id, PopperScreen screen) {
    return getAssetService().getGameMediaItem(id, screen);
  }

  @Override
  public PreferenceEntryRepresentation getPreference(String key) {
    return this.preferencesServiceClient.getPreference(key);
  }

  @Override
  public List<RankedPlayerRepresentation> getRankedPlayers() {
    return getPlayerService().getRankedPlayers();
  }

  public RestClient getRestClient() {
    return restClient;
  }


  public void clearDiscordCache() {
    restClient.clearCache("discord/");
  }


  /*********************************************************************************************************************
   * Utils
   */
  public void download(@NonNull String url, @NonNull File target) throws Exception {
    RestTemplate template = new RestTemplate();
    LOG.info("HTTP Download " + restClient.getBaseUrl() + VPinStudioClientService.API + url);
    File file = template.execute(restClient.getBaseUrl() + VPinStudioClientService.API + url, HttpMethod.GET, null, clientHttpResponse -> {
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

  public ObservedProperties getProperties(String propertiesName) {
    if (!observedProperties.containsKey(propertiesName)) {
      Map<String, Object> result = restClient.get(VPinStudioClientService.API + "properties/" + propertiesName, Map.class);
      Properties properties = new Properties();
      properties.putAll(result);
      ObservedProperties observedProperties = new ObservedProperties(propertiesName, properties);
      observedProperties.setObserver(this);
      this.observedProperties.put(propertiesName, observedProperties);
    }

    return this.observedProperties.get(propertiesName);
  }

  public String getURL(String segment) {
    if (!segment.startsWith("http") && !segment.contains(VPinStudioClientService.API)) {
      return restClient.getBaseUrl() + VPinStudioClientService.API + segment;
    }
    return segment;
  }

  @Override
  public void changed(String propertiesName, String key, Optional<String> updatedValue) {
    try {
      Map<String, Object> model = new HashMap<>();
      model.put(key, updatedValue.get());
      Boolean result = restClient.put(VPinStudioClientService.API + "properties/" + propertiesName, model);
      ObservedProperties obsprops = this.observedProperties.get(propertiesName);
      obsprops.notifyChange(key, updatedValue.get());
    } catch (Exception e) {
      LOG.error("Failed to upload changed properties: " + e.getMessage(), e);
    }
  }

  public void clearCache() {
    getDiscordService().clearCache();
    getImageCache().clearCache();
    getGameService().clearCache();
    getSystemService().clearCache();
    getAltSoundService().clearCache();
    getAltColorService().clearCache();
    getPupPackService().clearCache();
    getRomService().clearCache();
  }

  public void clearWheelCache() {
    getImageCache().clearWheelCache();
  }
}
