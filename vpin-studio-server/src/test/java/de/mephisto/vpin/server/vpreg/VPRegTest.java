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

  public static final String VPX = "Batman 66.vpx";
  public static final String VPREG_FILE = "C:\\vPinball\\VisualPinball\\User\\VPReg.stg";

  @Autowired
  private GameService gameService;

  @Test
  public void readFile() {
    File file = new File(VPREG_FILE);
    Game game = gameService.getGameByFilename(VPX);

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
  public void fullReadAndRestore() {
    File file = new File(VPREG_FILE);
    Game game = gameService.getGameByFilename(VPX);

    VPReg reg = new VPReg(file, game);
    String data = reg.toJson();
    reg.restore(data);
    String restoredData = reg.toJson();
    assertEquals(data, restoredData);
  }
}
