package de.mephisto.vpin.server.highscores.parsing.nvram;

import de.mephisto.vpin.restclient.highscores.DefaultHighscoresTitles;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.ScoreListFactory;
import de.mephisto.vpin.server.pinemhi.PINemHiService;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class NvRamOutputToScoreTextTest {
  private final static Logger LOG = LoggerFactory.getLogger(NvRamOutputToScoreTextTest.class);


  private final static List<String> ignoreList = Arrays.asList("kiko_a10.nv");

  @Test
  public void testAllFiles() throws Exception {
    File testFolder = new File("../testsystem/vPinball/VisualPinball/VPinMAME/nvram/");
    PINemHiService.adjustVPPathForEmulator(testFolder, getPinemhiIni(), true);

    ScoringDB scoringDB = ScoringDB.load();
    File[] files = testFolder.listFiles((dir, name) -> name.endsWith(".nv"));
    int count = 0;
    for (File entry : files) {
      if (ignoreList.contains(entry.getName())) {
        continue;
      }

      String baseName = FilenameUtils.getBaseName(entry.getName());
      if (!scoringDB.getSupportedNvRams().contains(baseName) || scoringDB.getNotSupported().contains(baseName)) {
        continue;
      }

      LOG.info("Reading '" + entry.getName() + "'");
      String raw = NvRamOutputToScoreTextConverter.convertNvRamTextToMachineReadable(getPinemhiExe(), entry);

      LOG.info(raw);

      assertNotNull(raw);
      List<Score> parse = ScoreListFactory.create(raw, new Date(entry.length()), null, DefaultHighscoresTitles.DEFAULT_TITLES);
      assertFalse(parse.isEmpty(), "Found empty highscore for nvram " + entry.getAbsolutePath());
      LOG.info("Parsed " + parse.size() + " score entries.");
      LOG.info("*******************************************************************************************");
      count++;
    }
    LOG.info("Tested " + count + " entries");
  }

  @Test
  public void testSingle() throws Exception {
    File testFolder = new File("../testsystem/vPinball/VisualPinball/VPinMAME/nvram/");
    // Set the path to this GameEmulator so that nv files can be found
    PINemHiService.adjustVPPathForEmulator(testFolder, getPinemhiIni(), true);

    File entry = new File(testFolder, "kiko_a10.nv");
    String raw = NvRamOutputToScoreTextConverter.convertNvRamTextToMachineReadable(getPinemhiExe(), entry);

    LOG.info(raw);

    assertNotNull(raw);
    assertEquals(raw, "HIGHEST SCORES\n" +
        "1) DAK    3.032.500\n" +
        "2) DAK    2.665.940\n" +
        "3) DAK    1.856.200\n" +
        "4) DAK    1.067.570");
    List<Score> parse = ScoreListFactory.create(raw, new Date(entry.length()), null, DefaultHighscoresTitles.DEFAULT_TITLES);
    LOG.info("Parsed " + parse.size() + " score entries.");

    for (Score score : parse) {
      LOG.info("Score: {}", score);
    }
    assertFalse(parse.isEmpty());
  }

  //-----------------

  private File getPinemhiIni() {
    return new File("../resources/pinemhi", PINemHiService.PINEMHI_INI);
  }

  private File getPinemhiExe() {
    return new File("../resources/pinemhi", PINemHiService.PINEMHI_COMMAND);
  }
}
