package de.mephisto.vpin.server.vpreg;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.util.vpreg.VPReg;
import de.mephisto.vpin.server.util.vpreg.VPRegScoreSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VPRegTest extends AbstractVPinServerTest {

  @Autowired
  private GameService gameService;

  @Test
  public void readFile() {
    File file = new File("C:\\vPinball\\VisualPinball\\User\\VPReg.stg");
//    Game game = gameService.getGameByFilename("Harry Potter.vpx");
    Game game = gameService.getGameByFilename("Batman 66.vpx");

    VPReg reg = new VPReg(file, game);
    VPRegScoreSummary summary = reg.readHighscores();
    String initialRaw = summary.toRaw();
    reg.resetHighscores();
    reg.restoreHighscore(summary);
    VPRegScoreSummary resettedSummary = reg.readHighscores();
    assertEquals(initialRaw, resettedSummary.toRaw());
    assertFalse(summary.getScores().isEmpty());
  }

  @Test
  public void fullRestore() {
    File file = new File("C:\\vPinball\\VisualPinball\\User\\VPReg.stg");
//    Game game = gameService.getGameByFilename("Harry Potter.vpx");
    Game game = gameService.getGameByFilename("Batman 66.vpx");

    VPReg reg = new VPReg(file, game);
    String data = reg.toJson();
  }
}
