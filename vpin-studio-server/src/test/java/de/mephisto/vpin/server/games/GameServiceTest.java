package de.mephisto.vpin.server.games;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GameServiceTest extends AbstractVPinServerTest {

  @Test
  public void testGames() {
    assertFalse(gameService.getGames().isEmpty());
    assertNotNull(gameService.getGameByFilename(AbstractVPinServerTest.EM_TABLE_NAME));
    assertFalse(gameService.getGamesByRom(EM_ROM_NAME).isEmpty());
    assertNotNull(gameService.getRecentHighscores(1));

    List<Game> games = gameService.getGames();
    for (Game game : games) {
      assertNotNull(gameService.scanGame(game.getId()));
      assertNotNull(gameService.getGame(game.getId()));
      assertTrue(gameService.getRomValidations(game).isEmpty());
      assertNotNull(gameService.getScores(game.getId()));
      assertNotNull(gameService.getScoreHistory(game.getId()));
    }
  }
}
