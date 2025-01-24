package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.highscores.parsing.ScoreListFactory;
import de.mephisto.vpin.server.games.Game;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ScoreListFactoryTest {

  private static ScoringDB scoringDB = ScoringDB.load();

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
    assertEquals(parse.size(), 5);
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
    assertEquals(parse.size(), 10);
  }
}
