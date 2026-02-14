package de.mephisto.vpin.commons.fx.cards;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import de.mephisto.vpin.restclient.cards.CardData;
import de.mephisto.vpin.restclient.system.FileLoaderForTest;


/**
 * TEMP CLASS FOR TESTING FOR TIME BEING
 */
public class CardDataMock  {

  public static CardData create() throws IOException {
    CardData data = new CardData();
    data.setGameName("Jaws (Original 2019)");
    data.setManufacturer("Original");
    data.setYear(2019);

    File wheelIcon = FileLoaderForTest.load("testsystem/vPinball/PinUPSystem/POPMedia/Visual Pinball X/Wheel/Jaws (Animated).apng");
    File backglass = FileLoaderForTest.load("testsystem/vPinball/PinUPSystem/POPMedia/Visual Pinball X/DMD/Jaws.png");
    System.out.println(wheelIcon.getAbsolutePath());
    data.setWheel(FileUtils.readFileToByteArray(wheelIcon));
    data.setBackground(FileUtils.readFileToByteArray(backglass));
  
    /*
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
    */

    return data;
  }

}
