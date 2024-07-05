package de.mephisto.vpin.restclient.preferences;

public class AutoFillSettings {
  private boolean gameVersion = true;
  private boolean gameYear = true;
  private boolean gameType = true;
  private boolean gameTheme = true;
  private boolean manufacturer = true;
  private boolean numberOfPlayers = true;
  private boolean author = true;
  private boolean category = true;
  private boolean ipdbNumber = true;
  private boolean url = true;
  private boolean designBy = true;
  private boolean notes = true;
  private boolean tags = true;
  private boolean details = true;

  public boolean isDetails() {
    return details;
  }

  public void setDetails(boolean details) {
    this.details = details;
  }

  public boolean isGameVersion() {
    return gameVersion;
  }

  public void setGameVersion(boolean gameVersion) {
    this.gameVersion = gameVersion;
  }

  public boolean isNumberOfPlayers() {
    return numberOfPlayers;
  }

  public void setNumberOfPlayers(boolean numberOfPlayers) {
    this.numberOfPlayers = numberOfPlayers;
  }

  public boolean isGameYear() {
    return gameYear;
  }

  public void setGameYear(boolean gameYear) {
    this.gameYear = gameYear;
  }

  public boolean isGameType() {
    return gameType;
  }

  public void setGameType(boolean gameType) {
    this.gameType = gameType;
  }

  public boolean isGameTheme() {
    return gameTheme;
  }

  public void setGameTheme(boolean gameTheme) {
    this.gameTheme = gameTheme;
  }

  public boolean isManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(boolean manufacturer) {
    this.manufacturer = manufacturer;
  }

  public boolean isAuthor() {
    return author;
  }

  public void setAuthor(boolean author) {
    this.author = author;
  }

  public boolean isCategory() {
    return category;
  }

  public void setCategory(boolean category) {
    this.category = category;
  }

  public boolean isIpdbNumber() {
    return ipdbNumber;
  }

  public void setIpdbNumber(boolean ipdbNumber) {
    this.ipdbNumber = ipdbNumber;
  }

  public boolean isUrl() {
    return url;
  }

  public void setUrl(boolean url) {
    this.url = url;
  }

  public boolean isDesignBy() {
    return designBy;
  }

  public void setDesignBy(boolean designBy) {
    this.designBy = designBy;
  }

  public boolean isNotes() {
    return notes;
  }

  public void setNotes(boolean notes) {
    this.notes = notes;
  }

  public boolean isTags() {
    return tags;
  }

  public void setTags(boolean tags) {
    this.tags = tags;
  }
}
