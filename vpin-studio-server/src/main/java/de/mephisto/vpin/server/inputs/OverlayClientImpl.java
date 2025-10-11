package de.mephisto.vpin.server.inputs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.OverlayClient;
import de.mephisto.vpin.restclient.alx.AlxSummary;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.restclient.client.ImageCache;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.frontend.FrontendMedia;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.GameScoreValidation;
import de.mephisto.vpin.restclient.games.GameStatus;
import de.mephisto.vpin.restclient.highscores.ScoreListRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.players.RankedPlayerRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.restclient.system.FeaturesInfo;
import de.mephisto.vpin.restclient.system.MonitorInfo;
import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.alx.AlxService;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.RankedPlayer;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.VPinScreenService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.GameStatusService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.highscores.cards.CardTemplatesService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.PngFrameCapture;
import de.mephisto.vpin.server.vps.VpsService;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OverlayClientImpl implements OverlayClient, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayClientImpl.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private GameService gameService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private GameStatusService gameStatusService;

  @Autowired
  private AlxService alxService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private VPinScreenService vPinScreenService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private AssetService assetService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private CardService cardService;

  @Autowired
  private CardTemplatesService cardTemplatesService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private VpsService vpsService;

  private ObjectMapper mapper;

  private final Map<String, byte[]> imageByteCache = new HashMap<>();
  private final ImageCache imageCache = new ImageCache(null);


  private <R> R convert(Object source, Class<R> clazz) {
    try {
      String s = mapper.writeValueAsString(source);
      return mapper.readValue(s, clazz);
    }
    catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return null;
  }

  private <R> List<R> convertList(List<?> source, Class<R[]> clazz) {
    try {
      String s = mapper.writeValueAsString(source);
      R[] array = mapper.readValue(s, clazz);
      return List.of(array);
    }
    catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  @Override
  public DiscordServer getDiscordServer(long serverId) {
    // FIXME why mapper ? 
    try {
      DiscordServer server = discordService.getServer(serverId);
      String s = mapper.writeValueAsString(server);
      return mapper.readValue(s, DiscordServer.class);
    }
    catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public String getURL(String segment) {
    if (!segment.startsWith("http") && !segment.contains(VPinStudioClient.API)) {
      return "http://localhost:" + SystemUtil.getPort() + "/" + VPinStudioClient.API + segment;
    }
    return segment;
  }

  public InputStream getCachedUrlImage(String imageUrl) {
    try {
      if (!imageByteCache.containsKey(imageUrl)) {
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
        imageByteCache.put(imageUrl, bytes);
        LOG.info("Cached image URL " + imageUrl);
      }
    }
    catch (IOException e) {
      LOG.error("Failed to read image from URL: " + e.getMessage(), e);
    }

    byte[] bytes = imageByteCache.get(imageUrl);
    return new ByteArrayInputStream(bytes);
  }

  @Override
  public InputStream getPersistentCachedUrlImage(String cache, String url) {
    try {
      String asset = url.substring(url.lastIndexOf("/") + 1, url.length());
      File folder = new File("./resources/cache/" + cache + "/");
      if (!folder.exists()) {
        folder.mkdirs();
      }
      File file = new File(folder, asset);
      if (file.exists()) {
        return new FileInputStream(file);
      }

      InputStream in = imageCache.getCachedUrlImage(url);
      if (in != null) {
        FileOutputStream out = new FileOutputStream(file);
        IOUtils.copy(in, out);
        LOG.info("Persisted for cache '" + cache + "': " + file.getAbsolutePath());
        in.close();
        out.close();
      }

      if (file.exists()) {
        return new FileInputStream(file);
      }
    }
    catch (Exception e) {
      LOG.error("Caching error: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public List<CompetitionRepresentation> getFinishedCompetitions(int limit) {
    List<Competition> finishedCompetitions = competitionService.getFinishedCompetitions(limit);
    return convertList(finishedCompetitions, CompetitionRepresentation[].class);
  }

  @Override
  public List<CompetitionRepresentation> getIScoredSubscriptions() {
    List<Competition> finishedCompetitions = competitionService.getIScoredSubscriptions();
    return convertList(finishedCompetitions, CompetitionRepresentation[].class);
  }

  @Override
  public CompetitionRepresentation getActiveCompetition(CompetitionType type) {
    Competition competition = competitionService.getActiveCompetition(type);
    return convert(competition, CompetitionRepresentation.class);
  }

  @Override
  public VpsTable getVpsTable(String tableId) {
    return vpsService.getTableById(tableId);
  }

  @Override
  public VpsTableVersion getVpsTableVersion(@Nullable String tableId, @Nullable String versionId) {
    return vpsService.getVpsVersion(tableId, versionId);
  }

  @Override
  public GameRepresentation getGameByVpsId(@Nullable String vpsTableId, @Nullable String vpsTableVersionId) {
    Game game = gameService.getGameByVpsTable(vpsTableId, vpsTableVersionId);
    if (game == null) {
      return null;
    }
    return getGame(game.getId());
  }

  @Override
  public GameRepresentation getGame(int id) {
    Game game = gameService.getGame(id);
    return convert(game, GameRepresentation.class);
  }

  @Override
  public GameRepresentation getGameCached(int id) {
    return getGame(id);
  }

  @Override
  public GameEmulatorRepresentation getGameEmulator(int emulatorId) {
    GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
    return convert(emulator, GameEmulatorRepresentation.class);
  }

  @Override
  public GameScoreValidation getGameScoreValidation(int gameId) {
    return gameService.getGameScoreValidation(gameId);
  }

  @Override
  public AlxSummary getAlxSummary(int gameId) {
    return alxService.getAlxSummary(gameId);
  }

  @Override
  public CardTemplate getCardTemplate(GameRepresentation game) {
    Game g = gameService.getGame(game.getId());
    return cardTemplatesService.getTemplateForGame(g);
  }

  @Override
  public CardData getCardData(GameRepresentation game, CardTemplate template) {
    Game _game = gameService.getGame(game.getId());
    return cardService.getCardData(_game, template, true);
  }

  //--------------------------

  @Override
  public FrontendMediaRepresentation getFrontendMedia(int id) {
    FrontendMedia frontendMedia = frontendService.getGameMedia(id);
    return convert(frontendMedia, FrontendMediaRepresentation.class);
  }

  @Override
  public ScoreListRepresentation getCompetitionScoreList(long id) {
    ScoreList competitionScores = competitionService.getCompetitionScores(id);
    return convert(competitionScores, ScoreListRepresentation.class);
  }

  @Override
  public ByteArrayInputStream getCompetitionBackground(long gameId) {
    Asset asset = assetService.getCompetitionBackground(gameId);
    if (asset != null) {
      return new ByteArrayInputStream(asset.getData());
    }
    return null;
  }

  @Override
  public ByteArrayInputStream getAsset(AssetType assetType, String uuid) {
    Asset asset = assetService.getByUuid(uuid);
    return new ByteArrayInputStream(asset.getData());
  }

  @Override
  public ByteArrayInputStream getWheelIcon(int id, boolean skipApng) {
    try {
      FrontendMediaItem defaultMediaItem = frontendService.getGameMedia(id).getDefaultMediaItem(VPinScreen.Wheel);
      if (defaultMediaItem != null && defaultMediaItem.getFile().exists()) {
        File file = defaultMediaItem.getFile();

        if (skipApng) {
          byte[] bytes = PngFrameCapture.captureFirstFrame(file);
          return new ByteArrayInputStream(bytes);
        }

        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] bytes = IOUtils.toByteArray(fileInputStream);
        fileInputStream.close();
        return new ByteArrayInputStream(bytes);
      }
    }
    catch (Exception e) {
      LOG.error("Error reading media item: " + e.getMessage(), e);
    }
    return null;
  }

  //---------------------------

  @Override
  public ScoreSummaryRepresentation getCompetitionScore(long id) {
    ScoreSummary competitionScore = competitionService.getCompetitionScore(id);
    return convert(competitionScore, ScoreSummaryRepresentation.class);
  }

  @Override
  public ScoreSummaryRepresentation getRecentScores(int count) {
    ScoreSummary summary = gameService.getRecentHighscores(count);
    return convert(summary, ScoreSummaryRepresentation.class);
  }

  @Override
  public ScoreSummaryRepresentation getRecentScoresByGame(int count, int gameId) {
    ScoreSummary summary = gameService.getRecentHighscores(count, gameId);
    return convert(summary, ScoreSummaryRepresentation.class);
  }

  @Override
  public MonitorInfo getScreenInfo(int id) {
    List<MonitorInfo> monitorInfos = systemService.getMonitorInfos();
    for (MonitorInfo monitorInfo : monitorInfos) {
      if (id == -1 && monitorInfo.isPrimary()) {
        return monitorInfo;
      }
      else if (monitorInfo.getId() == id) {
        return monitorInfo;
      }
    }
    return null;
  }

  //--------------------------

  @Override
  public void clearPreferenceCache() {
    // no server cache
  }

  @Override
  public PreferenceEntryRepresentation getPreference(String key) {
    Object preferenceValue = preferencesService.getPreferenceValue(key);
    PreferenceEntryRepresentation entry = new PreferenceEntryRepresentation();
    entry.setKey(key);
    entry.setValue(String.valueOf(preferenceValue));
    return entry;
  }

  @Override
  public <T> T getJsonPreference(String key, Class<T> clazz) {
    return preferencesService.getJsonPreference(key, clazz);
  }

  @Override
  public List<RankedPlayerRepresentation> getRankedPlayers() {
    List<RankedPlayer> rankedPlayers = highscoreService.getPlayersByRanks();
    return convertList(rankedPlayers, RankedPlayerRepresentation[].class);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }


  @Override
  public FrontendPlayerDisplay getScreenDisplay(VPinScreen screen) {
    return vPinScreenService.getScreenDisplay(screen);
  }

  //---------------------

  @Override
  public GameStatus startPause() {
    return gameStatusService.startPause();
  }

  @Override
  public GameStatus getPauseStatus() {
    return gameStatusService.getStatus();
  }

  @Override
  public GameStatus finishPause() {
    return gameStatusService.finishPause();
  }

  @Override
  public FeaturesInfo getFeatures() {
    return VPinStudioServer.Features;
  }
}
