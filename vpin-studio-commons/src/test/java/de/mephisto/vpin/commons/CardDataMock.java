package de.mephisto.vpin.commons;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import de.mephisto.vpin.commons.fx.cards.CardData;

/**
 * TEMP CLASS FOR TESTING FOR TIME BEING
 */
public class CardDataMock implements CardData {

  public String getGameDisplayName() {
    return "Ace of Speed (Original 2019)";
  }

  public File getWheelImage() {
    return new File("C:\\PinballX\\Media\\Visual Pinball\\Wheel Images\\Ace Of Speed (Original 2019).png");
  }

  public List<String> getScores() {
    ArrayList<String> scores = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      String score = i + ".  PL" + i + "   ";
      score += StringUtils.leftPad(Integer.toString(4000000 * i + RandomUtils.nextInt(0, 1000000)), 10);
      if (i == 2 || i== 3) {
        score = CardData.MARKER_EXTERNAL_SCORE + score;
      }
      scores.add(score);
    }
    return scores;
  }

  public String getRawScore() {
    return "ALE 10.000.000\nDHL  6.000.000\nOLE  2.458.366\nNUL    500.000";
  }

  public File getBackgroundImage() {
    return new File("C:\\PinballX\\Media\\Visual Pinball\\Backglass Images\\AC-DC (Stern 2012).png");
  }

}
