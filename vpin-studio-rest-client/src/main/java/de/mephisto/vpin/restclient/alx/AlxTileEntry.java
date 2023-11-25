package de.mephisto.vpin.restclient.alx;

public class AlxTileEntry {
  private final String title;
  private final String description;
  private final String value;

  public AlxTileEntry(String title, String description, String value) {
    this.title = title;
    this.description = description;
    this.value = value;
  }

  public String getTitle() {
    return title;
  }

  public String getValue() {
    return value;
  }

  public String getDescription() {
    return description;
  }
}
