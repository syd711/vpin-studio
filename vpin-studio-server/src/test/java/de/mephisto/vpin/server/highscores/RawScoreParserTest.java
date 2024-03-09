package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.highscores.DefaultHighscoresTitles;
import de.mephisto.vpin.server.highscores.parsing.RawScoreParser;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RawScoreParserTest {

  private String RAW3 = "GRAND CHAMPION\n" +
    "SLL      52.000.000\n" +
    "\n" +
    "HIGHEST SCORES\n" +
    "1) BRE      44.000.000\n" +
    "2) LFS      40.000.000\n" +
    "3) ZAP      36.000.000\n" +
    "4) RCF      32.000.000\n" +
    "\n" +
    "CASTLE CHAMPION\n" +
    "JCY - 6\n" +
    "CASTLES DESTROYED\n" +
    "\n" +
    "JOUST CHAMPION\n" +
    "DWF - 5\n" +
    "JOUST VICTORIES\n" +
    "\n" +
    "CATAPULT CHAMPION\n" +
    "ASR - 5\n" +
    "CATAPULT SLAMS\n" +
    "\n" +
    "PEASANT CHAMPION\n" +
    "BCM - 5\n" +
    "PEASANT REVOLTS\n" +
    "\n" +
    "DAMSEL CHAMPION\n" +
    "DJW - 5\n" +
    "DAMSELS SAVED\n" +
    "\n" +
    "TROLL CHAMPION\n" +
    "JCD - 20\n" +
    "TROLLS DESTROYED\n" +
    "\n" +
    "MADNESS CHAMPION\n" +
    "KOZ      20.000.000\n" +
    "\n" +
    "KING OF THE REALM\n" +
    "1) KOP\n" +
    "CROWNED FOR THE 1st TIME\n" +
    "16 AUG, 2022 7:16 PM\n";

  @Test
  public void testRawScoreParser() {
    RawScoreParser parser = new RawScoreParser(RAW3, new Date(), -1, DefaultHighscoresTitles.DEFAULT_TITLES);
    List<Score> parse = parser.parse();
    assertEquals(parse.size(), 5);
  }
}
