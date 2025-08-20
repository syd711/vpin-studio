package de.mephisto.vpin.restclient.cards;

import java.util.List;

import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;


/**
 */
public class CardData  {

  private int gameId;
  private String gameDisplayName;
  private String gameName;

  private String vpsName;
  private String manufacturer;
  private Integer year;
  private String wheelUrl;
  private String backgroundUrl;
  private String vpsTableId;

  private List<ScoreRepresentation> scores;
  private String rawScore;

  public void addBaseUrl(String baseurl) {
    wheelUrl = baseurl + wheelUrl;
    backgroundUrl = baseurl + backgroundUrl;
  }

  //-----------------------------------------

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getGameDisplayName() {
    return gameDisplayName;
  }

  public void setGameDisplayName(String gameDisplayName) {
    this.gameDisplayName = gameDisplayName;
  }

  public String getGameName() {
    return gameName;
  }

  public void setGameName(String gameName) {
    this.gameName = gameName;
  }

  public String getVpsName() {
    return vpsName;
  }

  public void setVpsName(String vpsName) {
    this.vpsName = vpsName;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
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

  public List<ScoreRepresentation> getScores() {
    return scores;
  }

  public void setScores(List<ScoreRepresentation> scores) {
    this.scores = scores;
  }

  public String getRawScore() {
    return rawScore;
  }

  public void setRawScore(String rawScore) {
    this.rawScore = rawScore;
  }

  public String getVpsTableId() {
    return vpsTableId;
  }

  public void setVpsTableId(String vpsTableId) {
    this.vpsTableId = vpsTableId;
  }
}
