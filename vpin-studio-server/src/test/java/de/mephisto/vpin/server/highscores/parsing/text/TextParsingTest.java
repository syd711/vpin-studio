package de.mephisto.vpin.server.highscores.parsing.text;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.highscores.parsing.ScoreParsingSummary;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPReg;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FilenameFilter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TextParsingTest {
  @Test
  public void testAllTextFiles() {
    ScoringDB scoringDB = ScoringDB.load();
    File folder = new File("../testsystem/vPinball/VisualPinball/User/");
    File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt") && !FilenameUtils.getBaseName(name).endsWith("LUT"));
    int count = 0;
    for (File entry : files) {
//      if (scoringDB.getIgnoredVPRegEntries().contains(entry)) {
//        continue;
//      }

//      System.out.println("Reading '" + entry.getName() + "'");
//      ScoreParsingSummary vpRegScoreSummary = regEntry.readHighscores();
//      assertNotNull(vpRegScoreSummary, "Reading failed for " + entry);
//      assertFalse(vpRegScoreSummary.getScores().isEmpty(), "No score entry found for " + entry);
//      assertNotNull(vpRegScoreSummary.getScores().get(0).getInitials(), "No score initials found for " + entry);
//      count++;
//      assertNotEquals(0, vpRegScoreSummary.getScores().get(0).getScore(), "No score record found for " + entry);
    }
    System.out.println("Tested " + count + " entries");
  }
}
