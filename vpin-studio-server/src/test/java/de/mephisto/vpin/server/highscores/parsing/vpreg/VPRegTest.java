package de.mephisto.vpin.server.highscores.parsing.vpreg;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.ScoreListFactory;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class VPRegTest {

  private static ScoringDB scoringDB = ScoringDB.load();

  @Test
  public void readFile() {
    File vpRegFile = new File("../testsystem/vPinball/VisualPinball/User/VPReg.stg");

    VPReg reg = new VPReg(vpRegFile, "JAWSHighScore", AbstractVPinServerTest.VPREG_TABLE_NAME);
    ScoreParsingSummary summary = reg.readHighscores();
    String initialRaw = summary.toRaw();
    reg.resetHighscores(99);

    ScoreParsingSummary resettedSummary = reg.readHighscores();
//    resettedSummary.getScores().stream().forEach(s -> s.setInitials(""));
//    assertEquals(initialRaw, resettedSummary.toRaw());
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
  public void testDelete() {
    File vpRegFile = new File("C:\\vPinball\\VisualPinball\\User\\VPReg.stg");

    VPReg reg = new VPReg(vpRegFile, "AMH", null);
    String data = reg.toJson();
    assertTrue(data != null);

    reg.deleteEntry("AMH");
  }

  @Test
  public void testAllHighscores() {
    ScoringDB scoringDB = ScoringDB.load();
    File vpRegFile = new File("../testsystem/vPinball/VisualPinball/User/VPReg2.stg");
    testVpRegFile(vpRegFile, scoringDB);
  }

  @Test
  public void testAllHighscores2() {
    ScoringDB scoringDB = ScoringDB.load();
    File vpRegFile = new File("../testsystem/vPinball/VisualPinball/User/VPReg-gorgatron.stg");
    testVpRegFile(vpRegFile, scoringDB);
  }

  @Test
  public void testAllHighscores3() {
    ScoringDB scoringDB = ScoringDB.load();
    File vpRegFile = new File("../testsystem/vPinball/VisualPinball/User/VPReg-iamondiscord.stg");

    testVpRegFile(vpRegFile, scoringDB);
  }

  @Test
  public void testSingleInitialsHighscores() {
    File vpRegFile = new File("../testsystem/vPinball/VisualPinball/User/VPReg-iamondiscord.stg");
    VPReg reg = new VPReg(vpRegFile, "nobs", null);
    ScoreParsingSummary vpRegScoreSummary = reg.readHighscores();
    System.out.println(vpRegScoreSummary.toRaw());
    assertNotNull(vpRegScoreSummary);
    assertFalse(vpRegScoreSummary.getScores().isEmpty(), "No score entry found for");
  }

  private static void testVpRegFile(File vpRegFile, ScoringDB scoringDB) {
    VPReg reg = new VPReg(vpRegFile);
    List<String> entries = reg.getEntries();
    int count = 0;
    for (String entry : entries) {
      if (entry.toLowerCase().endsWith("lut")) {
        continue;
      }

      if (scoringDB.getIgnoredVPRegEntries().contains(entry)) {
        continue;
      }

      VPReg regEntry = new VPReg(vpRegFile, entry, null);
      ScoreParsingSummary vpRegScoreSummary = regEntry.readHighscores();
      if (vpRegScoreSummary == null) {
        continue;
      }

      assertNotNull(vpRegScoreSummary, "Reading failed for " + entry);
      assertFalse(vpRegScoreSummary.getScores().isEmpty(), "No score entry found for " + entry);
      assertNotNull(vpRegScoreSummary.getScores().get(0).getInitials(), "No score initials found for " + entry);
      count++;

      List<Score> parse = ScoreListFactory.create(vpRegScoreSummary.toRaw(), new Date(), null, scoringDB);
      assertFalse(parse.isEmpty(), "No scores parsed for " + entry);
    }
    System.out.println("Tested " + count + " entries");
  }

  @Test
  public void testSingleHighscores() {
    File vpRegFile = new File("../testsystem/vPinball/VisualPinball/User/VPReg-iamondiscord.stg");

    VPReg reg = new VPReg(vpRegFile, "Mariner", null);
    ScoreParsingSummary vpRegScoreSummary = reg.readHighscores();
    System.out.println(vpRegScoreSummary.toRaw());
    assertNotNull(vpRegScoreSummary);
    assertFalse(vpRegScoreSummary.getScores().isEmpty(), "No score entry found for");
  }

}
