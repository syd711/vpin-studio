package de.mephisto.vpin.restclient.components;

public class AlxBarEntry {
  private final String title;
  private final String value;
  private final int percentage;
  private final String color;

  public AlxBarEntry(String title, String value, int percentage, String color) {
    this.title = title;
    this.value = value;
    this.percentage = percentage;
    this.color = color;
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
