package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.highscores.DefaultHighscoresTitles;
import de.mephisto.vpin.server.highscores.parsing.ScoreListFactory;
import de.mephisto.vpin.server.games.Game;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ScoreListFactoryTest {

  private String MM = "GRAND CHAMPION\n" +
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

  private String tf_130 = "\"AUTOBOT\r\n" +
      "GRAND CHAMPION\r\n" +
      "OPT        75.000.000\r\n" +
      "\r\n" +
      "AUTOBOT\r\n" +
      "HIGH SCORES\r\n" +
      "#1 JAZ        55.000.000\r\n" +
      "#2 PWL        40.000.000\r\n" +
      "#3 IRN        30.000.000\r\n" +
      "#4 BEE        25.000.000\r\n" +
      "\r\n" +
      "DECEPTICON\r\n" +
      "GRAND CHAMPION\r\n" +
      "MEG        75.000.000\r\n" +
      "\r\n" +
      "DECEPTICON\r\n" +
      "HIGH SCORES\r\n" +
      "#1 STR        55.000.000\r\n" +
      "#2 SND        40.000.000\r\n" +
      "#3 SHK        30.000.000\r\n" +
      "#4 BLK        25.000.000\r\n" +
      "\r\n" +
      "COMBO CHAMPION\r\n" +
      "LON   20 COMBOS\r\n" +
      "\r\n" +
      "BEST COMBO CHAMPION\r\n" +
      "LON   5-WAY\r\n";

  @Test
  public void testScoreListFactoryDefault() {

    List<Score> parse = ScoreListFactory.create(MM, new Date(), null, DefaultHighscoresTitles.DEFAULT_TITLES);
    assertEquals(parse.size(), 5);
  }

  @Test
  public void testScoreListFactorySortedScoreAdapter() {
    Game tf_130_game = new Game();
    tf_130_game.setRom("tf_180");

    List<Score> parse = ScoreListFactory.create(tf_130, new Date(), tf_130_game, DefaultHighscoresTitles.DEFAULT_TITLES);
    assertEquals(parse.size(), 10);
  }
}
