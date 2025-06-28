package de.mephisto.vpin.restclient.cards;

import java.util.List;


/**
 */
public class CardData  {

  public static final String MARKER_EXTERNAL_SCORE = "!^";

  private String gameDisplayName;
  private String wheelUrl;
  private String backgroundUrl;

  private List<String> scores;
  private String rawScore;

  public void addBaseUrl(String baseurl) {
    wheelUrl = baseurl + wheelUrl;
    backgroundUrl = baseurl + backgroundUrl;
  }

  public String getGameDisplayName() {
    return gameDisplayName;
  }

  public void setGameDisplayName(String gameDisplayName) {
    this.gameDisplayName = gameDisplayName;
  }

  public String getWheelUrl() {
    return wheelUrl;
  }

  public void setWheelUrl(String wheelUrl) {
    this.wheelUrl = wheelUrl;
  }

  public String getBackgroundUrl() {
    return backgroundUrl;
  }

  public void setBackgroundUrl(String backgroundUrl) {
    this.backgroundUrl = backgroundUrl;
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
