package de.mephisto.vpin.server.highscores.parsing.nvram;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.ScoreListFactory;
import de.mephisto.vpin.server.pinemhi.PINemHiService;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

public class NvRamOutputToScoreTextTest {
  private final static Logger LOG = LoggerFactory.getLogger(NvRamOutputToScoreTextTest.class);

  private static ScoringDB scoringDB = ScoringDB.load();

  private final static List<String> ignoreList = Arrays.asList("kiko_a10.nv", "dh_lx2.nv");

  @Test
  public void testAllFiles() throws Exception {
    File testFolder = new File("../testsystem/vPinball/VisualPinball/VPinMAME/nvram/");
    PINemHiService.adjustVPPathForEmulator(testFolder, getPinemhiIni(), true);

    ScoringDB scoringDB = ScoringDB.load();
    File[] files = testFolder.listFiles((dir, name) -> name.endsWith(".nv"));
    int count = 0;
    int created = 0;
    List<String> failedList = new ArrayList<>();
    for (File entry : files) {
//      if (!entry.getName().equals("simp_a27.nv")) {
//        continue;
//      }

      if (ignoreList.contains(entry.getName())) {
        continue;
      }

      String baseName = FilenameUtils.getBaseName(entry.getName());
      if (!scoringDB.getSupportedNvRams().contains(baseName) || scoringDB.getNotSupported().contains(baseName)) {
        continue;
      }

      LOG.info("Reading '" + entry.getName() + "'");
      String raw = NvRamOutputToScoreTextConverter.convertNvRamTextToMachineReadable(getPinemhiExe(), entry);

      assertNotNull(raw);
      List<Score> parse = ScoreListFactory.create(raw, new Date(entry.length()), null, scoringDB);
      assertFalse(parse.isEmpty(), "Found empty highscore for nvram " + entry.getAbsolutePath());

      File listFile = new File(entry.getAbsolutePath().concat(".list"));

      Locale loc = Locale.GERMANY;

      StringBuilder scoreList = new StringBuilder();
      for (Score score : parse) {
        scoreList.append("#" + score.getPosition() + " " + score.getPlayerInitials() + "   " + score.getFormattedScore(loc) + System.lineSeparator());
      }

      if (listFile.exists()) {
        // compare with test output
        String fileContents = Files.readString(listFile.toPath(), StandardCharsets.UTF_8);
        if (!fileContents.equals(scoreList.toString())) {
          failedList.add(entry.getName());
          System.out.println(fileContents);
          System.out.println(scoreList.toString());

          byte[] scBytes = scoreList.toString().getBytes();
          byte[] fcBytes = fileContents.getBytes();
          for (int i = 0; i < fcBytes.length && i < scBytes.length; i++) {
            if (scBytes[i] != fcBytes[i]) {
              System.out.println(scBytes[i] + "|" + fcBytes[i]);
            }
          }
        }
      }
      else {
        // create for next test
        listFile.createNewFile();
        try (FileWriter writer = new FileWriter(listFile, StandardCharsets.UTF_8)) {
          writer.write(scoreList.toString());
        }
        created++;
      }

      System.out.println("Parsed " + parse.size() + " score entries.");
      System.out.println("*******************************************************************************************");
      count++;
    }

    LOG.info("Tested " + count + " entries, " + failedList.size() + " failed, " + created + " new list files created.");
    for (String item : failedList) {
      System.out.println("  '" + item + "' failed.");
    }

    assertEquals("NVRam failed: " + failedList, 0, failedList.size());
  }

  /**
   * Test DefaultAdapter
   */
  @Test
  public void test_Single() throws Exception {
    doTestSingle("robo_a34.nv",
        "#1 JJJ   9,540,810\r\n" + //
            "#2 AAA   1,098,360\r\n" + //
            "#3 AAA   588,560\r\n" + //
            "#4 AAA   277,620");
  }

  /**
   * Test DefaultAdapter
   */
  @Test
  public void test_dh_lx2() throws Exception {
//    doTestSingle("dh_lx2.nv",
//        "#1 2.9   2,961,835,010\r\n" +
//            "#2 ???   1,507,170,530\r\n" +
//            "#3 DAD   1,167,255,510\r\n" +
//            "#4 DAD   1,050,455,400\r\n" +
//            "#5 DAD   1,016,950,110");
  }

  /**
   * Test SortedScoreAdapter
   */
  @Test
  public void test_TF_180() throws Exception {
    doTestSingle("tf_180.nv",
        "#1 DAD   101,548,900\r\n" + //
            "#2 EDY   93,114,900\r\n" + //
            "#3 OPT   75,000,000\r\n" + //
            "#4 MEG   75,000,000\r\n" + //
            "#5 DAD   69,372,610\r\n" + //
            "#6 JAZ   55,000,000\r\n" + //
            "#7 STR   55,000,000\r\n" + //
            "#8 DAD   48,505,120\r\n" + //
            "#9 PWL   40,000,000\r\n" + //
            "#10 SND   40,000,000");
  }

  /**
   * Test SortedScoreAdapter
   */
  @Test
  public void test_SingleScore() throws Exception {
//    doTestSingle("pool_l7.nv",
//        "#1 DAK   1.582.140\n" +
//            "#2 DAK   1.561.190\n" +
//            "#3 DAK   1.394.100\n" +
//            "#4 DAK   1.354.040\n" +
//            "#5 DAK   2.522.560\n" +
//            "#6 ???   455.320\n" +
//            "#7 BRE   0\n" +
//            "#8 XAQ   0");
//
//    doTestSingle("wrldtou2.nv",
//         "#1 ???   1,867,500");

    doTestSingle("kiko_a10.nv",
         "#1 DAK   3,032,500\r\n" +
             "#2 DAK   2,665,940\r\n" +
             "#3 DAK   1,856,200\r\n" +
             "#4 DAK   1,067,570\r\n");
  }


  protected void doTestSingle(String nv, String expected) throws Exception {
    Game game = new Game();
    game.setGameDisplayName("Dummy test game for " + nv);
    game.setRom(nv.replace(".nv", ""));

    File testFolder = new File("../testsystem/vPinball/VisualPinball/VPinMAME/nvram/");
    // Set the path to this GameEmulator so that nv files can be found
    PINemHiService.adjustVPPathForEmulator(testFolder, getPinemhiIni(), true);

    File entry = new File(testFolder, nv);
    String raw = NvRamOutputToScoreTextConverter.convertNvRamTextToMachineReadable(getPinemhiExe(), entry);
    assertNotNull(raw);

    LOG.info("raw : " + raw);

    List<Score> parse = ScoreListFactory.create(raw, new Date(entry.length()), game, scoringDB);
    LOG.info("Parsed " + parse.size() + " score entries.");

    StringBuilder scores = new StringBuilder();
    for (Score score : parse) {
      LOG.info("Score: {}", score);
      if (scores.length() > 0) {
        scores.append("\r\n");
      }
      scores.append(score.toString(Locale.US));
    }

    assertFalse(parse.isEmpty(), "Parsed scores is empty");
    if (expected != null) {
      assertEquals(expected.trim(), scores.toString().trim());
    }
  }

  // -----------------

  private File getPinemhiIni() {
    return new File("../resources/pinemhi", PINemHiService.PINEMHI_INI);
  }

  private File getPinemhiExe() {
    return new File("../resources/pinemhi", PINemHiService.PINEMHI_COMMAND);
  }
}
