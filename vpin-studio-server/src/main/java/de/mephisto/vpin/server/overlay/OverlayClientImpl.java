package de.mephisto.vpin.server.overlay;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.mephisto.vpin.restclient.OverlayClient;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.representations.*;
import de.mephisto.vpin.server.assets.Asset;
import de.mephisto.vpin.server.assets.AssetService;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.CompetitionService;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.ScoreList;
import de.mephisto.vpin.server.popper.Emulator;
import de.mephisto.vpin.server.popper.GameMedia;
import de.mephisto.vpin.server.popper.GameMediaItem;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Service
public class OverlayClientImpl implements OverlayClient, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(OverlayClientImpl.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private CompetitionService competitionService;

  @Autowired
  private AssetService assetService;

  private ObjectMapper mapper;

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
  public List<CompetitionRepresentation> getActiveOfflineCompetitions() {
    try {
      List<Competition> competitions = competitionService.getActiveOfflineCompetitions();
      String s = mapper.writeValueAsString(competitions);
      return List.of(mapper.readValue(s, CompetitionRepresentation[].class));
    } catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return Collections.emptyList();
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
  public ScoreSummaryRepresentation getGameScores(int id) {
    try {
      ScoreSummary scores = gameService.getScores(id);
      String s = mapper.writeValueAsString(scores);
      return mapper.readValue(s, ScoreSummaryRepresentation.class);
    } catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public GameMediaRepresentation getGameMedia(int id) {
    try {
      Game game = gameService.getGame(id);
      GameMedia gameMedia = game.getEmulator().getGameMedia();
      String s = mapper.writeValueAsString(gameMedia);
      return mapper.readValue(s, GameMediaRepresentation.class);
    } catch (Exception e) {
      LOG.error("Error during conversion: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public ScoreListRepresentation getCompetitionScores(long id) {
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
  public ByteArrayInputStream getCompetitionBackground(long gameId) {
    try {
      Asset asset = assetService.getCompetitionBackground(gameId);
      if (asset == null) {
        File background = new File(SystemService.RESOURCES, "competition-bg-default.png");
        FileInputStream fileInputStream = new FileInputStream(background);
        byte[] bytes = IOUtils.toByteArray(fileInputStream);
        fileInputStream.close();
        return new ByteArrayInputStream(bytes);
      }
      return new ByteArrayInputStream(asset.getData());
    } catch (IOException e) {
      LOG.error("Failed to read competition background: " + e.getMessage(), e);
    }
    return null;
  }

  @Override
  public ByteArrayInputStream getAsset(String uuid) {
    Asset asset = assetService.getByUuid(uuid);
    return new ByteArrayInputStream(asset.getData());
  }

  @Override
  public ByteArrayInputStream getGameMediaItem(int id, PopperScreen screen) {
    try {
      Game game = gameService.getGame(id);
      Emulator emulator = game.getEmulator();
      GameMedia gameMedia = emulator.getGameMedia();
      GameMediaItem gameMediaItem = gameMedia.get(screen);
      if (gameMediaItem != null) {
        File file = gameMediaItem.getFile();
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
  public void afterPropertiesSet() throws Exception {
    mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }
}
