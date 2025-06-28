package de.mephisto.vpin.commons;

import java.util.ArrayList;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import de.mephisto.vpin.restclient.cards.CardData;


/**
 * TEMP CLASS FOR TESTING FOR TIME BEING
 */
public class CardDataMock  {

  public static CardData create() {
    CardData data = new CardData();
    data.setGameDisplayName("Ace of Speed (Original 2019)");
    data.setWheelUrl("file:///C:/PinballX/Media/Visual Pinball/Wheel Images/Ace Of Speed (Original 2019).png");
    data.setBackgroundUrl("file:///C:/PinballX/Media/Visual Pinball/Backglass Images/AC-DC (Stern 2012).png");
  
    ArrayList<String> scores = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      String score = i + ".  PL" + i + "   ";
      score += StringUtils.leftPad(Integer.toString(4000000 * i + RandomUtils.nextInt(0, 1000000)), 10);
      if (i == 2 || i== 3) {
        score = CardData.MARKER_EXTERNAL_SCORE + score;
      }
      scores.add(score);
    }
    data.setScores(scores);
    data.setRawScore("ALE 10.000.000\nDHL  6.000.000\nOLE  2.458.366\nNUL    500.000");

    return data;
  }

}
