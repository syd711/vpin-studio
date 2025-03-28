package de.mephisto.vpin.server.highscores.parsing;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.text.TextHighscoreAdapters;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class TextParsingTest extends AbstractVPinServerTest {

  @Autowired
  private HighscoreParsingService highscoreParsingService;

  @Test
  public void testAllTextFiles() {
    ScoringDB scoringDB = ScoringDB.load();
    File folder = new File("../testsystem/vPinball/VisualPinball/User/");
    File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt") && !FilenameUtils.getBaseName(name).endsWith("LUT"));
    int count = 0;
    for (File entry : files) {
      HighscoreMetadata highscoreMetadata = new HighscoreMetadata();
      System.out.println("Reading '" + entry.getName() + "'");
      String raw = new TextHighscoreAdapters().convertTextFileTextToMachineReadable(highscoreMetadata, scoringDB, entry);
      assertNull(highscoreMetadata.getStatus());
      if (raw != null) {
        List<Score> scores = highscoreParsingService.parseScores(new Date(entry.lastModified()), raw, null, -1);
        assertNotNull(scores, "Reading failed for " + entry);
        assertFalse(scores.isEmpty(), "No score entry found for " + entry);
        assertNotNull(scores.get(0).getPlayerInitials(), "No score initials found for " + entry);
        assertNull(highscoreMetadata.getStatus());
      }
    }
    System.out.println("Tested " + count + " entries");
  }

  @Test
  public void testSingleTextFiles() {
    ScoringDB scoringDB = ScoringDB.load();
    File folder = new File("../testsystem/vPinball/VisualPinball/User/");
    File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt") && !FilenameUtils.getBaseName(name).endsWith("LUT"));
    int count = 0;
    for (File entry : files) {
      HighscoreMetadata highscoreMetadata = new HighscoreMetadata();
      if(entry.getName().equals("Route66.txt")) {
        System.out.println("Reading '" + entry.getName() + "'");
        String raw = new TextHighscoreAdapters().convertTextFileTextToMachineReadable(highscoreMetadata, scoringDB, entry);
        assertNull(highscoreMetadata.getStatus());
        if (raw != null) {
          List<Score> scores = highscoreParsingService.parseScores(new Date(entry.lastModified()), raw, null, -1);
          assertNotNull(scores, "Reading failed for " + entry);
          assertFalse(scores.isEmpty(), "No score entry found for " + entry);
          assertNotNull(scores.get(0).getPlayerInitials(), "No score initials found for " + entry);
          assertNull(highscoreMetadata.getStatus());
        }
        break;
      }
    }
    System.out.println("Tested " + count + " entries");
  }
}
