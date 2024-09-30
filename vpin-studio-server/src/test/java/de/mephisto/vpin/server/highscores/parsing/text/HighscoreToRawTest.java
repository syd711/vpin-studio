package de.mephisto.vpin.server.highscores.parsing.text;


import de.mephisto.vpin.restclient.highscores.DefaultHighscoresTitles;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.highscores.HighscoreMetadata;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.RawScoreParser;
import de.mephisto.vpin.server.highscores.parsing.text.adapters.ScoreTextFileAdapter;
import de.mephisto.vpin.server.highscores.parsing.text.adapters.ScoreTextFileAdapterImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static de.mephisto.vpin.server.highscores.parsing.text.TextHighscoreAdapters.adapters;
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
      String raw = new TextHighscoreAdapters().convertTextFileTextToMachineReadable(new HighscoreMetadata(), scoringDB, entry);

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
  public void testResetting() {
    ScoringDB scoringDB = ScoringDB.load();
    File folder = new File("../testsystem/vPinball/VisualPinball/User/");
    File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt") && !FilenameUtils.getBaseName(name).endsWith("LUT"));
    int count = 0;
    for (File entry : files) {
      if (scoringDB.getIgnoredTextFiles().contains(entry.getName())) {
        continue;
      }
      FileInputStream fileInputStream = null;
      try {
        fileInputStream = new FileInputStream(entry);
        List<String> lines = IOUtils.readLines(fileInputStream, Charset.defaultCharset());
        File resetFile = new File(entry.getParentFile(), entry.getName() + ".reset");
        if(!resetFile.exists()) {
          System.out.println("No reset file found for " + resetFile.getAbsolutePath());
        }
        assertTrue(resetFile.exists());
        String resettedTemplate = FileUtils.readFileToString(resetFile, StandardCharsets.UTF_8.name());

        for (ScoreTextFileAdapter adapter : adapters) {
          if (adapter.isApplicable(entry, lines)) {
            List<String> resettedLines = adapter.resetHighscore(entry, lines);

            String resetted = String.join("\r\n", resettedLines) + "\r\n";
            assertEquals(resettedTemplate, resetted, "Mismatch for " + entry.getName() + ", used adapter " + adapter.getClass().getSimpleName());
            break;
          }
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      finally {
        if (fileInputStream != null) {
          try {
            fileInputStream.close();
          }
          catch (IOException e) {
            //ignore
          }
        }
      }
    }
    System.out.println("Tested " + count + " entries");

  }

  @Test
  public void testSingle() {
    ScoringDB scoringDB = ScoringDB.load();
    File entry = new File("../testsystem/vPinball/VisualPinball/User/", "TeachersPet_65VPX.txt");
    System.out.println("Reading '" + entry.getName() + "'");
    String raw = new TextHighscoreAdapters().convertTextFileTextToMachineReadable(new HighscoreMetadata(), scoringDB, entry);

    System.out.println(raw);

    assertNotNull(raw);
    assertTrue(raw.contains(ScoreTextFileAdapterImpl.HIGHEST_SCORES));
  }
}
