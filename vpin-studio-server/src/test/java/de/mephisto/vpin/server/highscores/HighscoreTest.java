package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.VPinServerTest;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HighscoreTest extends VPinServerTest {

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private GameService gameService;

  @Test
  public void testHighscore() {
//    Game game = gameService.getGameByFilename("Game of Thrones LE (Stern 2015) VPW v1.0.1.vpx");
    Game game = gameService.getGameByFilename(VPinServerTest.TEST_GAME_FILENAME);
    ScoreSummary highscores = highscoreService.getHighscores(game.getId(), game.getGameDisplayName());

    assertNotNull(highscores);
    assertNotNull(highscores.getRaw());
    assertFalse(highscores.getScores().isEmpty());
  }

  /**
   * GRAND CHAMPION
   * SLL     7.500.000.000
   *
   * HIGHEST SCORES
   * 1) BRE     7.000.000.000
   * 2) LFS     6.500.000.000
   * 3) RCF     6.000.000.000
   * 4) DTW     5.500.000.000
   *
   * MARTIAN CHAMPION
   * LFS - 20
   * MARTIANS DESTROYED
   *
   * RULER OF THE UNIVERSE
   * TEX
   * INAUGURATED
   * 14 DEC, 2022 5:59 PM
   *
   * BUY-IN HIGHEST SCORES
   * 1) DWF     5.000.000.000
   * 2) ASR     4.500.000.000
   * 3) BCM     4.000.000.000
   * 4) MOO     3.500.000.000
   */
  @Test
  public void testHighscores() throws InterruptedException {
    Game game = gameService.getGameByFilename(VPinServerTest.TEST_GAME_FILENAME);
    ScoreSummary highscores = highscoreService.getHighscores(game.getId(), game.getGameDisplayName());

    assertNotNull(highscores);
    assertNotNull(highscores.getRaw());
    assertFalse(highscores.getScores().isEmpty());

    List<ScoreSummary> highscoresWithScore = highscoreService.getHighscoresWithScore();
    assertFalse(highscoresWithScore.isEmpty());


    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.YEAR, -10);
    Date from = cal.getTime();
    ScoreList scoresBetween = highscoreService.getScoresBetween(game.getId(), from, new Date());
    assertFalse(scoresBetween.getScores().isEmpty());

    HighscoreMetadata metadata = highscoreService.scanScore(game);
    String raw = metadata.getRaw();
    raw = raw.replace("BRE     7.000.000.000", "MFA     7.100.000.000");
    metadata.setRaw(raw);

    highscoreService.addHighscoreChangeListener(new HighscoreChangeListener() {
      @Override
      public void highscoreChanged(@NotNull HighscoreChangeEvent event) {
        assertNotNull(event.getNewScore());
        assertEquals("7.100.000.000", event.getNewScore().getScore());
        assertEquals("7.100.000.000", event.getNewScore().getScore());
        assertEquals(2, event.getNewScore().getPosition());
      }
    });
    highscoreService.updateHighscore(game, metadata);

    ScoreSummary recentHighscores = gameService.getRecentHighscores(10);
    assertFalse(recentHighscores.getScores().isEmpty());

    Score score = recentHighscores.getScores().get(0);
    assertEquals(score.getGameId(), game.getId());
    assertEquals("7.100.000.000", score.getScore());
    Thread.sleep(3000);
  }
}
