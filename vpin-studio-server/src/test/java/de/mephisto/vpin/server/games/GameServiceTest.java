package de.mephisto.vpin.server.games;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GameServiceTest extends AbstractVPinServerTest {

  @Test
  public void testGames() {
    List<Game> games = gameService.getKnownGames(1);
    assertFalse(games.isEmpty());
    // check the file is loaded
    assertNotNull(gameService.getGameByFilename(1, EM_TABLE_NAME));

    // force re-scan as state is uncertain
    for (Game game : games) {
      assertNotNull(gameService.scanGame(game.getId()));
      assertNotNull(gameService.getGame(game.getId()));
      assertNotNull(gameService.getScores(game.getId()));
      assertNotNull(gameService.getScoreHistory(game.getId()));
    }

    assertFalse(gameService.getGamesByRom(1, EM_ROM_NAME).isEmpty());
    assertNotNull(gameService.getRecentHighscores(1));
  }

  @Test
  public void testDirectB2S() {
    testDirectB2S("Jaws", "Jaws.directb2s");
    testDirectB2S("Baseball", "Baseball (1970).directb2s");
    // fail as not in popper database
    //testDirectB2S("250", "250 cc (Inder 1992)" + File.separatorChar + "250 cc (Inder 1992).directbs");
  }
  private void testDirectB2S(String gameName, String expectedB2sName) {
    Game game = gameService.findMatch(gameName);
    File gameFolder = game.getGameFile().getParentFile();

    File b2sFile = game.getDirectB2SFile();
    assertEquals(gameFolder, b2sFile.getParentFile());

    String b2sFileName = game.getDirectB2SFilename();
    assertEquals(expectedB2sName, b2sFileName);
  }
}
