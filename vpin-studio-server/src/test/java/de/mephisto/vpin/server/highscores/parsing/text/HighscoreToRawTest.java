package de.mephisto.vpin.server.highscores.parsing.text;


import de.mephisto.vpin.restclient.highscores.DefaultHighscoresTitles;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.RawScoreParser;
import de.mephisto.vpin.server.highscores.parsing.text.adapters.ScoreTextFileAdapterImpl;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HighscoreToRawTest {

  @Test
  public void testAllTextFiles() {
    ScoringDB scoringDB = ScoringDB.load();
    File folder = new File("../testsystem/vPinball/VisualPinball/User/");
    File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt") && !FilenameUtils.getBaseName(name).endsWith("LUT"));
    int count = 0;
    for (File entry : files) {
      if (scoringDB.getIgnoredTextFiles().contains(entry.getName())) {
        continue;
      }

      System.out.println("Reading '" + entry.getName() + "'");
      String raw = TextHighscoreToRawConverter.convertTextFileTextToMachineReadable(scoringDB, entry, "???");

      System.out.println(raw);

      assertNotNull(raw);
      assertTrue(raw.contains(ScoreTextFileAdapterImpl.HIGHEST_SCORES));

      RawScoreParser parser = new RawScoreParser(raw, new Date(entry.length()), -1, DefaultHighscoresTitles.DEFAULT_TITLES);
      List<Score> parse = parser.parse();
      assertFalse(parse.isEmpty());
    }
    System.out.println("Tested " + count + " entries");
  }

  @Test
  public void testSingle() {
    ScoringDB scoringDB = ScoringDB.load();
    File entry = new File("../testsystem/vPinball/VisualPinball/User/", "BallsAPoppin_56VPX.txt");
    System.out.println("Reading '" + entry.getName() + "'");
    String raw = TextHighscoreToRawConverter.convertTextFileTextToMachineReadable(scoringDB, entry, "???");

    System.out.println(raw);

    assertNotNull(raw);
    assertTrue(raw.contains(ScoreTextFileAdapterImpl.HIGHEST_SCORES));
  }
}
