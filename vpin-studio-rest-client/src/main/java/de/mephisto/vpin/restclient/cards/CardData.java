package de.mephisto.vpin.restclient.cards;

import java.io.File;
import java.util.List;


/**
 */
public class CardData  {

  public static final String MARKER_EXTERNAL_SCORE = "!^";

  private String gameDisplayName;
  private File wheelImage;
  private File backgroundImage;

  private List<String> scores;
  private String rawScore;


  public String getGameDisplayName() {
    return gameDisplayName;
  }

  public void setGameDisplayName(String gameDisplayName) {
    this.gameDisplayName = gameDisplayName;
  }

  public File getWheelImage() {
    return wheelImage;
  }

  public void setWheelImage(File wheelImage) {
    this.wheelImage = wheelImage;
  }

  public File getBackgroundImage() {
    return backgroundImage;
  }

  public void setBackgroundImage(File backgroundImage) {
    this.backgroundImage = backgroundImage;
  }

  public List<String> getScores() {
    return scores;
  }

  public void setScores(List<String> scores) {
    this.scores = scores;
  }

  public String getRawScore() {
    return rawScore;
  }

  public void setRawScore(String rawScore) {
    this.rawScore = rawScore;
  }
}
