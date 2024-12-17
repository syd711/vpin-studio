package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HighscoreParserTest extends AbstractVPinServerTest {

  private String RAW1 = "5 MULTIBALLS CHAMPS\n" +
    "1) JEK\n" +
    "2) JEK\n" +
    "3) JEK\n" +
    "4) JEK\n" +
    "5) JEK\n" +
    "\n" +
    "HIGHEST SCORES\n" +
    "#1  JOE            250.000.000\n" +
    "#2  JEK            225.000.000\n" +
    "#3  NF             200.000.000\n" +
    "#4  DAY            175.000.000\n" +
    "#5  ION            150.000.000\n" +
    "#6  KRT            125.000.000\n" +
    "#7  MFA            121.246.910\n" +
    "#8  JAY            120.000.000\n" +
    "#9  LON            115.000.000\n" +
    "#10 MT             110.000.000\n";

  private String RAW2 = "WORLD RECORD\n" +
    "JEK    7.500.000\n" +
    "\n" +
    "TODAY'S HI-SCORE\n" +
    "1) RAY    6.500.000\n" +
    "2) BLS    5.500.000\n" +
    "3) NBW    4.500.000\n" +
    "4) P G    3.500.000\n" +
    "5) A A    2.500.000\n" +
    "\n" +
    "LOOP BACK CHAMP\n" +
    "CHP   7\n";

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
  private String RAW4 = "AUTOBOT\n" +
    "GRAND CHAMPION\n" +
    "OPT        75.000.000\n" +
    "\n" +
    "AUTOBOT\n" +
    "HIGH SCORES\n" +
    "#1 JAZ        55.000.000\n" +
    "#2 PWL        40.000.000\n" +
    "#3 IRN        30.000.000\n" +
    "#4 BEE        25.000.000\n" +
    "\n" +
    "DECEPTICON\n" +
    "GRAND CHAMPION\n" +
    "MEG        75.000.000\n" +
    "\n" +
    "DECEPTICON\n" +
    "HIGH SCORES\n" +
    "#1 STR        55.000.000\n" +
    "#2 SND        40.000.000\n" +
    "#3 SHK        30.000.000\n" +
    "#4 BLK        25.000.000\n" +
    "\n" +
    "COMBO CHAMPION\n" +
    "LON   20 COMBOS\n" +
    "\n" +
    "BEST COMBO CHAMPION\n" +
    "LON   5-WAY\n";

  private String RAW5 = "GRAND CHAMPION\n" +
    "SSR     1.000.000.000\n" +
    "\n" +
    "Q CONTINUUM\n" +
    "1) TEX    16.000.000.000\n" +
    "2) XAQ    14.000.000.000\n" +
    "3) MAT    12.000.000.000\n" +
    "4) TED    10.000.000.000\n" +
    "\n" +
    "HONOR ROLL\n" +
    "1) DAN       800.000.000\n" +
    "2) ZAP       600.000.000\n" +
    "3) MFA       418.466.520\n" +
    "4) GER       400.000.000\n" +
    "\n" +
    "OFFICER'S CLUB\n" +
    "1) WAG     5.000.000.000\n" +
    "2) LED     2.000.000.000\n" +
    "3) WAL     1.000.000.000\n" +
    "4) JAP       500.000.000\n";


  private String singleton = "HIGHEST SCORES\n" +
    "1)       5.555.555\n" +
    "2)       4.000.000\n" +
    "3)       3.000.000\n" +
    "4)       2.000.000";

  private String orderCheck = "AUTOBOT\n" +
    "GRAND CHAMPION\n" +
    "OPT        75.000.000\n" +
    "\n" +
    "AUTOBOT\n" +
    "HIGH SCORES\n" +
    "#1 JAZ        55.000.000\n" +
    "#2 PWL        40.000.000\n" +
    "#3 IRN        30.000.000\n" +
    "#4 BEE        25.000.000\n" +
    "\n" +
    "DECEPTICON\n" +
    "GRAND CHAMPION\n" +
    "MEG        75.000.000\n" +
    "\n" +
    "DECEPTICON\n" +
    "HIGH SCORES\n" +
    "#1 STR        55.000.000\n" +
    "#2 SND        40.000.000\n" +
    "#3 SHK        30.000.000\n" +
    "#4 BLK        25.000.000\n" +
    "\n" +
    "COMBO CHAMPION\n" +
    "LON   20 COMBOS\n" +
    "\n" +
    "BEST COMBO CHAMPION\n" +
    "LON   5-WAY\n";

  private String blankValues = "CHAMPION\n" +
    "               0\n" +
    "\n" +
    "HIGHEST SCORES\n" +
    "1) JCY    6,000,000\n" +
    "2) JRK    5,500,000\n" +
    "3) CPG    5,000,000\n" +
    "4) PFZ    4,500,000";

  private String invalidTitle = "XCHAMPION\n" +
    "ABC            0\n" +
    "\n" +
    "HIGHEST SCORES\n" +
    "1) JCY    6,000,000\n" +
    "2) JRK    5,500,000\n" +
    "3) CPG    5,000,000\n" +
    "4) PFZ    4,500,000";

  private final List<String> testlings = Arrays.asList(RAW1, RAW2, RAW3, RAW4, RAW5);

  @Test
  public void testParsing() {
    for (String testling : testlings) {
      List<Score> test = highscoreParsingService.parseScores(new Date(), testling, null, -1l);
      assertFalse(test.isEmpty());
      assertTrue(test.size() > 3);
    }
  }

  @Test
  public void testRaw() {
    List<Score> test = highscoreParsingService.parseScores(new Date(), RAW3, null, -1l);
    assertFalse(test.isEmpty());
    assertEquals(test.size(), 5);
  }

  @Test
  public void testSingleton() {
    List<Score> test = highscoreParsingService.parseScores(new Date(), singleton, null, -1l);
    assertFalse(test.isEmpty());
    assertTrue(test.size() > 3);
  }

  @Test
  public void testInvalidTitle() {
    List<Score> test = highscoreParsingService.parseScores(new Date(), invalidTitle, null, -1l);
    assertFalse(test.isEmpty());
    assertTrue(test.size() > 3);
  }

  @Test
  public void testBlankScores() {
    List<Score> test = highscoreParsingService.parseScores(new Date(), blankValues, null, -1l);
    assertFalse(test.isEmpty());
    assertTrue(test.size() > 3);
  }

  @Test
  public void testOrder() {
    List<Score> test = highscoreParsingService.parseScores(new Date(), orderCheck, null, -1l);
    assertFalse(test.isEmpty());
    assertTrue(test.size() > 3);

    Score score = test.get(0);
    assertEquals("75.000.000", score.getScore());
    assertEquals("OPT", score.getPlayerInitials());

    score = test.get(1);
    assertEquals("55.000.000", score.getScore());
    assertEquals("JAZ", score.getPlayerInitials());

    score = test.get(2);
    assertEquals("40.000.000", score.getScore());
    assertEquals("PWL", score.getPlayerInitials());

    score = test.get(3);
    assertEquals("30.000.000", score.getScore());
    assertEquals("IRN", score.getPlayerInitials());

    score = test.get(4);
    assertEquals("25.000.000", score.getScore());
    assertEquals("BEE", score.getPlayerInitials());

  }
}
