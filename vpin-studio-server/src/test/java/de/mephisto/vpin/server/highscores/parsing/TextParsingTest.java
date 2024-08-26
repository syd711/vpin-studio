package de.mephisto.vpin.server.highscores.parsing;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.text.TextHighscoreConverters;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
      System.out.println("Reading '" + entry.getName() + "'");
      String raw = TextHighscoreConverters.convertTextFileTextToMachineReadable(scoringDB, entry);
      if (raw != null) {
        List<Score> scores = highscoreParsingService.parseScores(new Date(entry.lastModified()), raw, -1, -1);
        assertNotNull(scores, "Reading failed for " + entry);
        assertFalse(scores.isEmpty(), "No score entry found for " + entry);
        assertNotNull(scores.get(0).getPlayerInitials(), "No score initials found for " + entry);
      }
    }
    System.out.println("Tested " + count + " entries");
  }
}
