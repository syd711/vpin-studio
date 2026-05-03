package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.highscores.parsing.ScoreListFactory;
import de.mephisto.vpin.server.highscores.parsing.nvram.NvRamParsingConfiguration;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.system.SystemService;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ScoreListFactoryTest {

  private static ScoringDB scoringDB = ScoringDB.load();

  static {
    try {
      SystemService.RESOURCES = "../resources/";
      NvRamParsingConfiguration conf = new NvRamParsingConfiguration();
      conf.afterPropertiesSet();
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testParse() {
    Game game = new Game();
    game.setRom("mj_130");

    String rawScore = 
            "MVP\n" +
            "#1 MJJ     200.000.000\n" +
            "\n" +
            "DUNK CHAMP\n" +
            "#2 JVJ     175.000.000\n" +
            "\n" +
            "3 PT. CHAMP\n" +
            "#3 JMJ     150.000.000\n" +
            "\n" +
            "SCORE CHAMP\n" +
            "#4 MJJ     125.000.000\n" +
            "\n" +
            "STEAL CHAMP\n" +
            "#5 DJ      100.000.000\n" +
            "\n" +
            "SIXTH MAN\n" +
            "#6 EDY      81.105.540";

    List<Score> parse = ScoreListFactory.create(rawScore, new Date(), game, scoringDB);
    assertEquals(6, parse.size());
  }


  @Test
  public void testScoreListFactoryDefaultAdapter() {
    Game game = new Game();
    game.setRom("mm_109c");

    String rawScore = "GRAND CHAMPION\n" +
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

    List<Score> parse = ScoreListFactory.create(rawScore, new Date(), game, scoringDB);
    assertEquals(5, parse.size());
  }

  @Test
  public void testScoreListFactorySortedScoreAdapter() {
    Game game = new Game();
    game.setRom("tf_180");

    String rawScore = "\"AUTOBOT\n" +
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

    List<Score> parse = ScoreListFactory.create(rawScore, new Date(), game, scoringDB);
    assertEquals(10, parse.size());
  }

  @Test
  public void testScoreListFactoryDefaultAdapterAll() {
    Game game = new Game();
    game.setRom("afm_113b");

    String rawScore = "GRAND CHAMPION\n" +
      "MFA       282 764 610\n" +
      "\n" +
      "HIGHEST SCORES\n" +
      "1) BBB       277 437 190\n" +
      "2) AAA       271 668 560\n" +
      "3) CCC       220 466 160\n" +
      "4) SLL       100 000 000\n" +
      "\n" +
      "MARTIAN CHAMPION\n" +
      "LFS - 20\n" +
      "MARTIANS DESTROYED\n" +
      "\n" +
      "RULER OF THE UNIVERSE\n" +
      "TEX\n" +
      "INAUGURATED\n" +
      "24 SEP, 2023 1:27 PM\n" +
      "\n" +
      "BUY-IN HIGHEST SCORES\n" +
      "1) DWF     5 000 000 000\n" +
      "2) ASR     4 500 000 000\n" +
      "3) BCM     4 000 000 000\n" +
      "4) MOO     3 500 000 000";

    List<Score> parse = ScoreListFactory.create(rawScore, new Date(), game, scoringDB);
    assertEquals(5, parse.size());
    assertScore("HIGHEST SCORES", "BBB", 277437190, null, parse.get(1));

    parse = ScoreListFactory.create(rawScore, new Date(), game, scoringDB, true);
    assertEquals(10, parse.size());
    assertScore("HIGHEST SCORES", "AAA", 271668560, null, parse.get(2));
    assertScore("MARTIAN CHAMPION", "LFS", 20, " MARTIANS DESTROYED", parse.get(5));
    assertScore("BUY-IN HIGHEST SCORES", "DWF", 5000000000L, null, parse.get(6));
  }

  @Test
  public void testScoreListFactoryDefaultAdapterMultipleChampions() {
    Game game = new Game();
    game.setRom("bdk_294");

    String rawScore = "GRAND CHAMPION\n"  +
      "EDY       293 023 150\n"  +
      "\n"  +
      "HIGH SCORES\n"  +
      "#1 GAG       240 000 000\n"  +
      "#2 LFS       200 000 000\n"  +
      "#3 AGE       163 785 490\n"  +
      "#4 M G       160 000 000\n"  +
      "\n"  +
      "BATMOBILE HURRY-UP CHAMPION\n"  +
      "AGE        10 287 880\n"  +
      "\n"  +
      "BAT SIGNAL CHALLENGE CHAMPION\n"  +
      "M R        25 000 000\n"  +
      "\n"  +
      "GOTHAM CITY CHAMPION\n"  +
      "JLS        75 000 000";

    List<Score> parse = ScoreListFactory.create(rawScore, new Date(), game, scoringDB);
    assertEquals(parse.size(), 5);
    assertScore("HIGH SCORES", "GAG", 240000000, null, parse.get(1));

    parse = ScoreListFactory.create(rawScore, new Date(), game, scoringDB, true);
    assertEquals(parse.size(), 8);
    assertScore("HIGH SCORES", "GAG", 240000000, null, parse.get(1));
    assertScore("BATMOBILE HURRY-UP CHAMPION", "AGE", 10287880, null, parse.get(5));
    assertScore("BAT SIGNAL CHALLENGE CHAMPION", "M R", 25000000, null, parse.get(6));
    assertScore("GOTHAM CITY CHAMPION", "JLS", 75000000, null, parse.get(7));
  }

  @Test
  public void testScoreListFactoryDefaultAdapterNoScores() {
    Game game = new Game();
    game.setRom("wd_12");

    String rawScore = "GRAND CHAMPION\n" +
      "DAD     3.762.579.380\n" +
      "\n" +
      "HIGHEST SCORES\n" +
      "1) DAD     3.696.155.350\n" +
      "2) DAD     3.536.278.110\n" +
      "3) BBB     2.157.734.760\n" +
      "4) BSO     1.750.000.000\n" +
      "\n" +
      "THE ROOF CHAMPION\n" +
      "BBB\n" +
      "\n" +
      "MIDNIGHT CHAMP\n" +
      "XAQ";

    List<Score> parse = ScoreListFactory.create(rawScore, new Date(), game, scoringDB);
    assertEquals(parse.size(), 5);
    assertScore("HIGHEST SCORES", "DAD", 3696155350L, null, parse.get(1));

    parse = ScoreListFactory.create(rawScore, new Date(), game, scoringDB, true);
    assertEquals(parse.size(), 5);
    assertScore("HIGHEST SCORES", "DAD", 3696155350L, null, parse.get(1));
    //assertScore("THE ROOF CHAMPION", "BBB", 0, null, parse.get(5));  // a score value is needed in the regexp
  }

  @Test
  public void testScoreListFactoryDefaultAdapterReplayScore() {
    Game game = new Game();
    game.setRom("surfnsaf");

    String rawScore = "REPLAY SCORE\n" +
      "19.500.000\n" +
      "\n" +
      "TOP WATER SLIDERS\n" +
      "1) TIM   101.000.000\n" +
      "2) RJW    81.000.000\n" +
      "3) JON    61.000.000\n" +
      "4) DAV    41.000.000\n" +
      "5) S K    21.000.000\n" +
      "\n" +
      "RAPIDS RECORD\n" +
      "CGB - 9";

    List<Score> parse = ScoreListFactory.create(rawScore, new Date(), game, scoringDB);
    assertEquals(5, parse.size());
    assertScore("TOP WATER SLIDERS", "TIM", 101000000, null, parse.get(0));

    parse = ScoreListFactory.create(rawScore, new Date(), game, scoringDB, true);
    assertEquals(6, parse.size());
    assertScore("TOP WATER SLIDERS", "TIM", 101000000, null, parse.get(0));
    assertScore("RAPIDS RECORD", "CGB", 9, null, parse.get(5));
  }

    @Test
  public void testScoreListFactoryDefaultAdapterWithUnits() {
    Game game = new Game();
    game.setRom("st_161h");

    String rawScore = "GRAND CHAMPION\n" +
      "SSR        75.000.000\n" +
      "\n" +
      "HIGH SCORES\n" +
      "#1 EDY        58.248.630\n" +
      "#2 LON        55.000.000\n" +
      "#3 GGF        40.000.000\n" +
      "#4 JMR        30.000.000\n" +
      "\n" +
      "COMBO CHAMPION\n" +
      "W   12 COMBOS\n" +
      "\n" +
      "WARP CHAMPION\n" +
      "MDK   6 WARPS\n" +
      "\n" +
      "MEDALS CHAMPION\n" +
      "XAQ   5M";

    List<Score> parse = ScoreListFactory.create(rawScore, new Date(), game, scoringDB);
    assertEquals(5, parse.size());

    parse = ScoreListFactory.create(rawScore, new Date(), game, scoringDB, true);
    assertEquals(8, parse.size());
    assertScore("GRAND CHAMPION", "SSR", 75000000, null, parse.get(0));
    assertScore("HIGH SCORES", "LON", 55000000, null, parse.get(2));
    assertScore("COMBO CHAMPION", "W  ", 12, " COMBOS", parse.get(5));
    assertScore("MEDALS CHAMPION", "XAQ", 5, "M", parse.get(7));
  }

  private void assertScore(String title, String initials, long value, String unit, Score score) {
    assertEquals(title, score.getLabel());
    assertEquals(initials, score.getPlayerInitials());
    assertEquals(value, score.getScore());
    if (StringUtils.isNotEmpty(unit)) {
      assertEquals(unit, score.getSuffix());
    }
  }
}
