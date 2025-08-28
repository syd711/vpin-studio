package de.mephisto.vpin.restclient.cards;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.mephisto.vpin.restclient.highscores.ScoreRepresentation;


/**
 */
public class CardData {

  private int gameId;
  private String gameDisplayName;
  private String gameName;

  private String vpsName;
  private String manufacturer;
  private Integer year;
  private String vpsTableId;

  private List<ScoreRepresentation> scores;
  private String rawScore;

  private byte[] background;
  private byte[] wheel;
  private byte[] manufacturerLogo;
  private byte[] otherMedia;

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

  //---------------------------------------

  public byte[] getWheel() {
    return wheel;
  }

  public void setWheel(byte[] wheel) {
    this.wheel = wheel;
  }

  @JsonIgnore
  public byte[] getBackground() {
    return background;
  }

  public void setBackground(byte[] background) {
    this.background = background;
  }

  public byte[] getManufacturerLogo() {
    return manufacturerLogo;
  }

  public void setManufacturerLogo(byte[] manufacturerLogo) {
    this.manufacturerLogo = manufacturerLogo;
  }

  public byte[] getOtherMedia() {
    return otherMedia;
  }

  public void setOtherMedia(byte[] other2Media) {
    this.otherMedia = other2Media;
  }
}
