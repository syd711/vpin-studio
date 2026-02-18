package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.highscores.HighscoreFiles;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.competitions.RankedPlayer;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.listeners.EventOrigin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HighscoreServiceTest extends AbstractVPinServerTest {

  @BeforeAll
  public void setup() {
    setupSystem();
  }

  @Test
  public void testGetHighscoreFiles() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game, "Game not found: " + tableName);

      HighscoreFiles files = highscoreService.getHighscoreFiles(game);
      assertNotNull(files);
    }
  }

  @Test
  public void testGetScoreSummary() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game);

      ScoreSummary summary = highscoreService.getScoreSummary(0, game);
      assertNotNull(summary);
    }
  }

  @Test
  public void testScanScore() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game);

      HighscoreMetadata metadata = highscoreService.scanScore(game, EventOrigin.USER_INITIATED);
      assertNotNull(metadata);
    }
  }

  @Test
  public void testReadHighscore() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game);

      HighscoreMetadata metadata = highscoreService.readHighscore(game);
      assertNotNull(metadata);
    }
  }

  @Test
  public void testGetScoreHistory() {
    for (String tableName : TABLE_NAMES) {
      Game game = gameService.getGameByFilename(1, tableName);
      assertNotNull(game);

      ScoreList history = highscoreService.getScoreHistory(game);
      assertNotNull(history);
    }
  }

  @Test
  public void testGetHighscoresWithScore() {
    List<ScoreSummary> summaries = highscoreService.getHighscoresWithScore();
    assertNotNull(summaries);
  }

  @Test
  public void testGetPlayersByRanks() {
    List<RankedPlayer> players = highscoreService.getPlayersByRanks();
    assertNotNull(players);
  }
}
