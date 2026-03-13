package de.mephisto.vpin.connectors.wovp.models;

public class PinballTable {
  private String id;
  private String externalId;
  private String name;
  private String manufacturer;
  private int players;
  private int status;
  private int year;
  private String tableType;
  private BackglassImage backglassImage;

  public BackglassImage getBackglassImage() {
    return backglassImage;
  }

  public void setBackglassImage(BackglassImage backglassImage) {
    this.backglassImage = backglassImage;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public int getPlayers() {
    return players;
  }

  public void setPlayers(int players) {
    this.players = players;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public String getTableType() {
    return tableType;
  }

  public void setTableType(String tableType) {
    this.tableType = tableType;
  }
}
