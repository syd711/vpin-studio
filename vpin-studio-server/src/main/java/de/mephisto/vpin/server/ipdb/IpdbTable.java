package de.mephisto.vpin.server.ipdb;

public class IpdbTable {

  private String id;
  private String name;
  private String ipdbUrl;
  private String manufacturer;
  private int players;
  private String type;
  private int year;
  private String theme;

  public String getDisplayName() {
    String result = this.name;
    if (this.manufacturer != null && this.manufacturer.trim().length() > 0) {
      result = result + " (" + this.manufacturer;
    }

    if (this.year > 0) {
      result = result + " " + this.year + ")";
    }
    else {
      result = result + ")";
    }

    return result;
  }

  //------------------------------

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getIpdbUrl() {
    return ipdbUrl;
  }

  public void setIpdbUrl(String ipdbUrl) {
    this.ipdbUrl = ipdbUrl;
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public String getTheme() {
    return theme;
  }

  public void setTheme(String theme) {
    this.theme = theme;
  }
}
