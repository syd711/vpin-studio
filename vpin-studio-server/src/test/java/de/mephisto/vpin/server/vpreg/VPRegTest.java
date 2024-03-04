package de.mephisto.vpin.server.vpreg;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.highscores.vpreg.VPReg;
import de.mephisto.vpin.server.highscores.vpreg.VPRegScoreSummary;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class VPRegTest {

  @Test
  public void readFile() {
    File vpRegFile = new File("../testsystem/vPinball/VisualPinball/User/VPReg.stg");

    VPReg reg = new VPReg(vpRegFile, "JAWSHighScore", AbstractVPinServerTest.VPREG_TABLE_NAME);
    VPRegScoreSummary summary = reg.readHighscores();
    String initialRaw = summary.toRaw();
    reg.resetHighscores();

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

  @Test
  public void testAllHighscores() {
    ScoringDB scoringDB = ScoringDB.load();
    File vpRegFile = new File("../testsystem/vPinball/VisualPinball/User/VPReg2.stg");
    VPReg reg = new VPReg(vpRegFile);
    List<String> entries = reg.getEntries();
    for (String entry : entries) {
      if (entry.toLowerCase().endsWith("lut")) {
        continue;
      }

      if (scoringDB.getIgnoredVPRegEntries().contains(entry)) {
        continue;
      }

      System.out.println("Reading '" + entry + "'");
      VPReg regEntry = new VPReg(vpRegFile, entry, null);
      VPRegScoreSummary vpRegScoreSummary = regEntry.readHighscores();
      assertNotNull(vpRegScoreSummary, "Reading failed for " + entry);
      assertFalse(vpRegScoreSummary.getScores().isEmpty(), "No score entry found for " + entry);
      assertNotNull(vpRegScoreSummary.getScores().get(0).getInitials(), "No score initials found for " + entry);

//      assertNotEquals(0, vpRegScoreSummary.getScores().get(0).getScore(), "No score record found for " + entry);
    }
  }

  @Test
  public void testSingleHighscores() {
    File vpRegFile = new File("../testsystem/vPinball/VisualPinball/User/VPReg2.stg");

    VPReg reg = new VPReg(vpRegFile, "volkan", null);
    VPRegScoreSummary vpRegScoreSummary = reg.readHighscores();
    assertNotNull(vpRegScoreSummary);
    assertFalse(vpRegScoreSummary.getScores().isEmpty(), "No score entry found for");
  }
}
