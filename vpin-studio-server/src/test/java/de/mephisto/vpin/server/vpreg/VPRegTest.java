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

public class VPRegTest {

  @Test
  public void readFile() {
    File vpRegFile = new File("../testsystem/vPinball/VisualPinball/User/VPReg.stg");

    VPReg reg = new VPReg(vpRegFile, "JAWSHighScore", AbstractVPinServerTest.VPREG_TABLE_NAME);
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
    File vpRegFile = new File("../testsystem/vPinball/VisualPinball/User/VPReg.stg");

    VPReg reg = new VPReg(vpRegFile, "JAWSHighScore", AbstractVPinServerTest.VPREG_TABLE_NAME);
    String data = reg.toJson();
    reg.restore(data);
    String restoredData = reg.toJson();
    assertEquals(data, restoredData);
  }
}
