package de.mephisto.vpin.server.inputs;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mephisto.vpin.connectors.vps.model.VpsTableVersion;
import de.mephisto.vpin.restclient.OverlayClient;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.ImageCache;
import de.mephisto.vpin.restclient.competitions.CompetitionRepresentation;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.discord.DiscordServer;
import de.mephisto.vpin.restclient.frontend.FrontendMedia;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreListRepresentation;
import de.mephisto.vpin.restclient.highscores.ScoreSummaryRepresentation;
import de.mephisto.vpin.restclient.players.RankedPlayerRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.RankedPlayer;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.preferences.PreferencesService;
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
  private GameService gameService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private AssetService assetService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private DiscordService discordService;

  @Autowired
  private VpsService vpsService;

  private ObjectMapper mapper;

  private final Map<String, byte[]> imageByteCache = new HashMap<>();
  private final ImageCache imageCache = new ImageCache(null);

  @Override
  public DiscordServer getDiscordServer(long serverId) {
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
    try {
      List<Competition> finishedCompetitions = competitionService.getFinishedCompetitions(limit);
      String s = mapper.writeValueAsString(finishedCompetitions);
      return List.of(mapper.readValue(s, CompetitionRepresentation[].class));
    }
    catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  @Override
  public List<CompetitionRepresentation> getIScoredSubscriptions() {
    try {
      List<Competition> finishedCompetitions = competitionService.getIScoredSubscriptions();
      String s = mapper.writeValueAsString(finishedCompetitions);
      return List.of(mapper.readValue(s, CompetitionRepresentation[].class));
    }
    catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  @Override
  public CompetitionRepresentation getActiveCompetition(CompetitionType type) {
    try {
      Competition competition = competitionService.getActiveCompetition(type);
      String s = mapper.writeValueAsString(competition);
      return mapper.readValue(s, CompetitionRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return null;
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
    try {
      Game game = gameService.getGame(id);
      String s = mapper.writeValueAsString(game);
      return mapper.readValue(s, GameRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public GameRepresentation getGameCached(int id) {
    return getGame(id);
  }

  @Override
  public FrontendMediaRepresentation getFrontendMedia(int id) {
    try {
      FrontendMedia frontendMedia = frontendService.getGameMedia(id);
      String s = mapper.writeValueAsString(frontendMedia);
      return mapper.readValue(s, FrontendMediaRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public ScoreListRepresentation getCompetitionScoreList(long id) {
    try {
      ScoreList competitionScores = competitionService.getCompetitionScores(id);
      String s = mapper.writeValueAsString(competitionScores);
      return mapper.readValue(s, ScoreListRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public ScoreSummaryRepresentation getCompetitionScore(long id) {
    try {
      ScoreSummary competitionScore = competitionService.getCompetitionScore(id);
      String s = mapper.writeValueAsString(competitionScore);
      return mapper.readValue(s, ScoreSummaryRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return null;
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
  public ByteArrayInputStream getGameMediaItem(int id, VPinScreen screen) {
    try {
      FrontendMediaItem defaultMediaItem = frontendService.getGameMedia(id).getDefaultMediaItem(screen);
      if (defaultMediaItem != null && defaultMediaItem.getFile().exists()) {
        File file = defaultMediaItem.getFile();
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

  @Override
  public ScoreSummaryRepresentation getRecentScores(int count) {
    try {
      ScoreSummary summary = gameService.getRecentHighscores(count);
      String s = mapper.writeValueAsString(summary);
      return mapper.readValue(s, ScoreSummaryRepresentation.class);
    }
    catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return null;
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
    try {
      List<RankedPlayer> rankedPlayers = highscoreService.getPlayersByRanks();
      String s = mapper.writeValueAsString(rankedPlayers);
      return List.of(mapper.readValue(s, RankedPlayerRepresentation[].class));
    }
    catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
