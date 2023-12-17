package de.mephisto.vpin.server.keyevent;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mephisto.vpin.restclient.OverlayClient;
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
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.RankedPlayer;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.discord.DiscordService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.popper.GameMediaItem;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.util.*;

@Service
public class OverlayClientImpl implements OverlayClient, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayClientImpl.class);

  @Autowired
  private GameService gameService;

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

  private ObjectMapper mapper;

  private final Map<String, byte[]> imageCache = new HashMap<>();

  @Override
  public DiscordServer getDiscordServer(long serverId) {
    try {
      DiscordServer server = discordService.getServer(serverId);
      String s = mapper.writeValueAsString(server);
      return mapper.readValue(s, DiscordServer.class);
    } catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return null;
  }

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

  @Override
  public List<CompetitionRepresentation> getFinishedCompetitions(int limit) {
    try {
      List<Competition> finishedCompetitions = competitionService.getFinishedCompetitions(limit);
      String s = mapper.writeValueAsString(finishedCompetitions);
      return List.of(mapper.readValue(s, CompetitionRepresentation[].class));
    } catch (Exception e) {
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
    } catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public GameRepresentation getGame(int id) {
    try {
      Game game = gameService.getGame(id);
      String s = mapper.writeValueAsString(game);
      return mapper.readValue(s, GameRepresentation.class);
    } catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public GameRepresentation getGameCached(int id) {
    return getGame(id);
  }

  @Override
  public ScoreListRepresentation getCompetitionScoreList(long id) {
    try {
      ScoreList competitionScores = competitionService.getCompetitionScores(id);
      String s = mapper.writeValueAsString(competitionScores);
      return mapper.readValue(s, ScoreListRepresentation.class);
    } catch (Exception e) {
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
    } catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public ByteArrayInputStream getCompetitionBackground(long gameId) {
    Asset asset = assetService.getCompetitionBackground(gameId);
    return new ByteArrayInputStream(asset.getData());
  }

  @Override
  public ByteArrayInputStream getAsset(AssetType assetType, String uuid) {
    Asset asset = assetService.getByUuid(uuid);
    return new ByteArrayInputStream(asset.getData());
  }

  @Override
  public ByteArrayInputStream getGameMediaItem(int id, PopperScreen screen) {
    try {
      Game game = gameService.getGame(id);
      GameMediaItem defaultMediaItem = game.getGameMedia().getDefaultMediaItem(screen);
      if (defaultMediaItem != null && defaultMediaItem.getFile().exists()) {
        File file = defaultMediaItem.getFile();
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] bytes = IOUtils.toByteArray(fileInputStream);
        fileInputStream.close();
        return new ByteArrayInputStream(bytes);
      }
    } catch (Exception e) {
      LOG.error("Error reading media item: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public ScoreSummaryRepresentation getRecentlyPlayedGames(int count) {
    try {
      ScoreSummary summary = gameService.getRecentHighscores(count);
      String s = mapper.writeValueAsString(summary);
      return mapper.readValue(s, ScoreSummaryRepresentation.class);
    } catch (Exception e) {
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
  public List<RankedPlayerRepresentation> getRankedPlayers() {
    try {
      List<RankedPlayer> rankedPlayers = highscoreService.getPlayersByRanks();
      String s = mapper.writeValueAsString(rankedPlayers);
      return List.of(mapper.readValue(s, RankedPlayerRepresentation[].class));
    } catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }
}
