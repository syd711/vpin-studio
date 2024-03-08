package de.mephisto.vpin.server.highscores.parsing.text;


import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.highscores.parsing.text.adapters.ScoreTextFileAdapterImpl;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
      String raw = HighscoreRawToMachineReadableConverter.convertTextFileTextToMachineReadable(entry);

      System.out.println(raw);

      assertNotNull(raw);
      assertTrue(raw.contains(ScoreTextFileAdapterImpl.HIGHEST_SCORES));
    }
    System.out.println("Tested " + count + " entries");
  }

  @Test
  public void testSingle() {
    File entry = new File("../testsystem/vPinball/VisualPinball/User/", "HangGlider_76VPX.txt");
    System.out.println("Reading '" + entry.getName() + "'");
    String raw = HighscoreRawToMachineReadableConverter.convertTextFileTextToMachineReadable(entry);

    System.out.println(raw);

    assertNotNull(raw);
    assertTrue(raw.contains(ScoreTextFileAdapterImpl.HIGHEST_SCORES));
  }
}
