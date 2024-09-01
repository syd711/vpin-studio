package de.mephisto.vpin.restclient.alx;

public class AlxBarEntry {
  private final String title;
  private final String value;
  private final int percentage;
  private final String color;
  private final int gameId;

  public AlxBarEntry(String title, String value, int percentage, String color, int gameId) {
    this.title = title;
    this.value = value;
    this.percentage = percentage;
    this.color = color;
    this.gameId = gameId;
  }

  public int getGameId() {
    return gameId;
  }

  public String getTitle() {
    return title;
  }

  public String getValue() {
    return value;
  }

  public String getColor() {
    return color;
  }

  public int getPercentage() {
    return percentage;
  }
}
