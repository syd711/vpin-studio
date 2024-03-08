package de.mephisto.vpin.server.highscores.parsing.nvram;


import de.mephisto.vpin.restclient.highscores.DefaultHighscoresTitles;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.RawScoreParser;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NvRamOutToRawTest {

  @Test
  public void testAllFiles() throws Exception {
    ScoringDB scoringDB = ScoringDB.load();
    File folder = new File("../testsystem/vPinball/VisualPinball/VPinMAME/nvram/");
    File[] files = folder.listFiles((dir, name) -> name.endsWith(".nv"));
    int count = 0;
    for (File entry : files) {
      String baseName = FilenameUtils.getBaseName(entry.getName());
      if (!scoringDB.getSupportedNvRams().contains(baseName) || scoringDB.getNotSupported().contains(baseName)) {
        continue;
      }

      System.out.println("Reading '" + entry.getName() + "'");
      String raw = NvRamHighscoreToRawConverter.convertNvRamTextToMachineReadable(new File("../resources/pinemhi/PINemHi.exe"), entry.getName());

      System.out.println(raw);

      assertNotNull(raw);
      RawScoreParser parser = new RawScoreParser(raw, new Date(entry.length()), -1, DefaultHighscoresTitles.DEFAULT_TITLES);
      List<Score> parse = parser.parse();
      assertFalse(parse.isEmpty());
      System.out.println("Parsed " + parse.size() + " score entries.");
      System.out.println("*******************************************************************************************");
      count++;
    }
    System.out.println("Tested " + count + " entries");
  }

  @Test
  public void testSingle() throws Exception {
    ScoringDB scoringDB = ScoringDB.load();
    File entry = new File("../testsystem/vPinball/VisualPinball/VPinMAME/nvram/", "sorcr_l1");
    String raw = NvRamHighscoreToRawConverter.convertNvRamTextToMachineReadable(new File("../resources/pinemhi/PINemHi.exe"), entry.getName());

    System.out.println(raw);

    assertNotNull(raw);
    RawScoreParser parser = new RawScoreParser(raw, new Date(entry.length()), -1, DefaultHighscoresTitles.DEFAULT_TITLES);
    List<Score> parse = parser.parse();
    System.out.println("Parsed " + parse.size() + " score entries.");
    assertFalse(parse.isEmpty());
  }
}
