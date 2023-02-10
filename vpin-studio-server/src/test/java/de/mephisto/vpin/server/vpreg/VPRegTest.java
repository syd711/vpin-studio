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
//    Game game = gameService.getGameByFilename("Stranger Things.vpx");
    Game game = gameService.getGameByFilename("Harry Potter.vpx");

    VPReg reg = new VPReg(file, game);
    VPRegScoreSummary summary = reg.readHighscores();
    System.out.println(summary.toRaw());
    reg.resetHighscores();
    VPRegScoreSummary resettedSummary = reg.readHighscores();
    System.out.println(resettedSummary.toRaw());

    reg.restoreHighscore(summary);
    resettedSummary = reg.readHighscores();
    System.out.println(resettedSummary.toRaw());

    assertFalse(summary.getScores().isEmpty());
  }
}
