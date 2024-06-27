package de.mephisto.vpin.server.highscores.parsing.nvram;

import de.mephisto.vpin.restclient.frontend.Emulator;
import de.mephisto.vpin.restclient.highscores.DefaultHighscoresTitles;
import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.highscores.Score;
import de.mephisto.vpin.server.highscores.parsing.RawScoreParser;
import de.mephisto.vpin.server.pinemhi.PINemHiService;

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
    // Emulator for test
    GameEmulator gameEmulator = getGameEmulator();

    // Set the path to this GameEmulator so that nv files can be found
    PINemHiService.adjustVPPathForEmulator(gameEmulator, getPinemhiIni(), true);

    // Emulator for test
    GameEmulator gameEmulator = getGameEmulator();

    // Set the path to this GameEmulator so that nv files can be found
    PINemHiService.adjustVPPathForEmulator(gameEmulator, getPinemhiIni(), true);

    ScoringDB scoringDB = ScoringDB.load();
    File folder = gameEmulator.getNvramFolder();
    File[] files = folder.listFiles((dir, name) -> name.endsWith(".nv"));
    int count = 0;
    for (File entry : files) {
      String baseName = FilenameUtils.getBaseName(entry.getName());
      if (!scoringDB.getSupportedNvRams().contains(baseName) || scoringDB.getNotSupported().contains(baseName)) {
        continue;
      }

      System.out.println("Reading '" + entry.getName() + "'");
      String raw = NvRamHighscoreToRawConverter.convertNvRamTextToMachineReadable(getPinemhiExe(), entry);

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
    // Emulator for test
    GameEmulator gameEmulator = getGameEmulator();

    // Set the path to this GameEmulator so that nv files can be found
    PINemHiService.adjustVPPathForEmulator(gameEmulator, getPinemhiIni(), true);
    
    File entry = new File(gameEmulator.getNvramFolder(), "godzilla.nv");
    String raw = NvRamHighscoreToRawConverter.convertNvRamTextToMachineReadable(getPinemhiExe(), entry);

    System.out.println(raw);

    assertNotNull(raw);
    RawScoreParser parser = new RawScoreParser(raw, new Date(entry.length()), -1, DefaultHighscoresTitles.DEFAULT_TITLES);
    List<Score> parse = parser.parse();
    System.out.println("Parsed " + parse.size() + " score entries.");
    assertFalse(parse.isEmpty());
  }
}
