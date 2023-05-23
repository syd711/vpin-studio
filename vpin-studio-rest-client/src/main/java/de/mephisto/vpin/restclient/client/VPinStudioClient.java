package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.*;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.representations.*;
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

  private final AltSoundService altSoundService;
  private final Archiving archiving;
  private final Assets assets;
  private final Competitions competitions;
  private final DirectB2S directB2S;
  private final Discord discord;
  private final Games games;
  private final HighscoreCards highscoreCards;
  private final ImageCache imageCache;
  private final IO io;
  private final Jobs jobs;
  private final Players players;
  private final Popper popper;
  private final Preferences preferences;
  private final Systm system;
  private final TableManager tableManager;
  private final Vpx vpx;

  public VPinStudioClient(String host) {
    this.host = host;
    restClient = RestClient.createInstance(host);

    this.altSoundService = new AltSoundService(this);
    this.archiving = new Archiving(this);
    this.assets = new Assets(this);
    this.competitions = new Competitions(this);
    this.directB2S = new DirectB2S(this);
    this.discord = new Discord(this);
    this.games = new Games(this);
    this.highscoreCards = new HighscoreCards(this);
    this.imageCache = new ImageCache(this);
    this.io = new IO(this);
    this.jobs = new Jobs(this);
    this.players = new Players(this);
    this.popper = new Popper(this);
    this.preferences = new Preferences(this);
    this.system = new Systm(this);
    this.tableManager = new TableManager(this);
    this.vpx = new Vpx(this);
  }

  public AltSoundService getAltSoundService() {
    return altSoundService;
  }

  public Archiving getArchiving() {
    return archiving;
  }

  public Assets getAssets() {
    return assets;
  }

  public Competitions getCompetitions() {
    return competitions;
  }

  public DirectB2S getDirectB2S() {
    return directB2S;
  }

  public Discord getDiscord() {
    return discord;
  }

  public Games getGames() {
    return games;
  }

  public HighscoreCards getHighscoreCards() {
    return highscoreCards;
  }

  public ImageCache getImageCache() {
    return imageCache;
  }

  public IO getIo() {
    return io;
  }

  public Jobs getJobs() {
    return jobs;
  }

  public Players getPlayers() {
    return players;
  }

  public Popper getPopper() {
    return popper;
  }

  public Preferences getPreferences() {
    return preferences;
  }

  public Systm getSystem() {
    return system;
  }

  public TableManager getTableManager() {
    return tableManager;
  }

  public Vpx getVpx() {
    return vpx;
  }


  @Override
  public DiscordServer getDiscordServer(long serverId) {
    return discord.getDiscordServer(serverId);
  }

  @Override
  public List<CompetitionRepresentation> getFinishedCompetitions(int limit) {
    return getCompetitions().getFinishedCompetitions(limit);
  }

  @Override
  public CompetitionRepresentation getActiveCompetition(CompetitionType type) {
    return getCompetitions().getActiveCompetition(type);
  }

  @Override
  public GameRepresentation getGame(int id) {
    return getGames().getGame(id);
  }

  @Override
  public InputStream getCachedUrlImage(String url) {
    return getImageCache().getCachedUrlImage(url);
  }

  @Override
  public ScoreSummaryRepresentation getCompetitionScore(long id) {
    return getCompetitions().getCompetitionScore(id);
  }

  @Override
  public ByteArrayInputStream getCompetitionBackground(long gameId) {
    return getCompetitions().getCompetitionBackground(gameId);
  }

  @Override
  public ScoreListRepresentation getCompetitionScoreList(long id) {
    return getCompetitions().getCompetitionScoreList(id);
  }

  @Override
  public ByteArrayInputStream getAsset(AssetType assetType, String uuid) {
    return getAssets().getAsset(assetType, uuid);
  }

  @Override
  public ScoreSummaryRepresentation getRecentlyPlayedGames(int count) {
    return getGames().getRecentlyPlayedGames(count);
  }

  @Override
  public ByteArrayInputStream getGameMediaItem(int id, PopperScreen screen) {
    return getAssets().getGameMediaItem(id, screen);
  }

  @Override
  public PreferenceEntryRepresentation getPreference(String key) {
    return this.preferences.getPreference(key);
  }

  @Override
  public List<RankedPlayerRepresentation> getRankedPlayers() {
    return getPlayers().getRankedPlayers();
  }

  public RestClient getRestClient() {
    return restClient;
  }

  public void clearTableCache() {
    restClient.clearCache("games/");
  }

  public void clearDiscordCache() {
    restClient.clearCache("discord/");
  }


  /*********************************************************************************************************************
   * Utils
   */
  public void download(@NonNull String url, @NonNull File target) throws Exception {
    RestTemplate template = new RestTemplate();
    LOG.info("HTTP Download " + restClient.getBaseUrl() + AbstractStudioClientModule.API + url);
    File file = template.execute(restClient.getBaseUrl() + AbstractStudioClientModule.API + url, HttpMethod.GET, null, clientHttpResponse -> {
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
      Map<String, Object> result = restClient.get(AbstractStudioClientModule.API + "properties/" + propertiesName, Map.class);
      Properties properties = new Properties();
      properties.putAll(result);
      ObservedProperties observedProperties = new ObservedProperties(propertiesName, properties);
      observedProperties.setObserver(this);
      this.observedProperties.put(propertiesName, observedProperties);
    }

    return this.observedProperties.get(propertiesName);
  }

  public String getURL(String segment) {
    if (!segment.startsWith("http") && !segment.contains(AbstractStudioClientModule.API)) {
      return restClient.getBaseUrl() + AbstractStudioClientModule.API + segment;
    }
    return segment;
  }

  @Override
  public void changed(String propertiesName, String key, Optional<String> updatedValue) {
    try {
      Map<String, Object> model = new HashMap<>();
      model.put(key, updatedValue.get());
      Boolean result = restClient.put(AbstractStudioClientModule.API + "properties/" + propertiesName, model);
      ObservedProperties obsprops = this.observedProperties.get(propertiesName);
      obsprops.notifyChange(key, updatedValue.get());
    } catch (Exception e) {
      LOG.error("Failed to upload changed properties: " + e.getMessage(), e);
    }
  }

  public void clearCache() {
    getImageCache().clearCache();
  }

  public void clearWheelCache() {
    getImageCache().clearWheelCache();
  }
}
