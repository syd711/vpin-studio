package de.mephisto.vpin.commons.fx.cards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;

import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;

/**
 * TEMP CLASS FOR TESTING FOR TIME BEING
 */
public class CardData {

  public String getGameDisplayName() {
    return "Ace of Speed (Original 2019)";
  }

  public File getWheelImage() {
    return new File("C:\\PinballX\\Media\\Visual Pinball\\Wheel Images\\Ace Of Speed (Original 2019).png");
  }

  public List<ScoreRepresentation> getScores() {
    ArrayList<ScoreRepresentation> scores = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      ScoreRepresentation score = new ScoreRepresentation();
      score.setPosition(i);
      score.setPlayerInitials("PL" + i);
      score.setScore(4000000 * i + RandomUtils.nextInt(0, 1000000));
      score.setExternal(i == 2 || i== 3);
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
