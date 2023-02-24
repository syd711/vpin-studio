package de.mephisto.vpin.restclient;

import java.util.HashMap;
import java.util.Map;

public class VpaManifest {
  private String emulatorType;
  private String gameName;
  private String gameFileName;
  private String gameDisplayName;
  private String gameTheme;
  private String notes;
  private int gameYear;
  private String romName;
  private String romUrl;
  private String manufacturer;
  private int numberOfPlayers;
  private String tags;
  private String category;
  private String author;
  private int volume;
  private String launchCustomVar;
  private String keepDisplays;
  private int gameRating;
  private String dof;
  private String IPDBNum;
  private String altRunMode;
  private String url;
  private String designedBy;

  private String uuid;
  private String tableName;
  private String icon;
  private String thumbnail;
  private String vpaFilename;
  private long vpaFileSize;
  private String vpaVersion;

  public long getVpaFileSize() {
    return vpaFileSize;
  }

  public void setVpaFileSize(long vpaFileSize) {
    this.vpaFileSize = vpaFileSize;
  }

  public String getVpaVersion() {
    return vpaVersion;
  }

  public void setVpaVersion(String vpaVersion) {
    this.vpaVersion = vpaVersion;
  }

  public String getVpaFilename() {
    return vpaFilename;
  }

  public void setVpaFilename(String vpaFilename) {
    this.vpaFilename = vpaFilename;
  }

  private VpaPackageInfo packageInfo;

  public VpaPackageInfo getPackageInfo() {
    return packageInfo;
  }

  public void setPackageInfo(VpaPackageInfo packageInfo) {
    this.packageInfo = packageInfo;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  private Map<String,Object> additionalData = new HashMap<>();

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public Map<String, Object> getAdditionalData() {
    return additionalData;
  }

  public void setAdditionalData(Map<String, Object> additionalData) {
    this.additionalData = additionalData;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getEmulatorType() {
    return emulatorType;
  }

  public void setEmulatorType(String emulatorType) {
    this.emulatorType = emulatorType;
  }

  public String getGameName() {
    return gameName;
  }

  public void setGameName(String gameName) {
    this.gameName = gameName;
  }

  public String getGameFileName() {
    return gameFileName;
  }

  public void setGameFileName(String gameFileName) {
    this.gameFileName = gameFileName;
  }

  public String getGameDisplayName() {
    return gameDisplayName;
  }

  public void setGameDisplayName(String gameDisplayName) {
    this.gameDisplayName = gameDisplayName;
  }

  public String getGameTheme() {
    return gameTheme;
  }

  public void setGameTheme(String gameTheme) {
    this.gameTheme = gameTheme;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public int getGameYear() {
    return gameYear;
  }

  public void setGameYear(int gameYear) {
    this.gameYear = gameYear;
  }

  public String getRomName() {
    return romName;
  }

  public void setRomName(String romName) {
    this.romName = romName;
  }

  public String getRomUrl() {
    return romUrl;
  }

  public void setRomUrl(String romUrl) {
    this.romUrl = romUrl;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public int getNumberOfPlayers() {
    return numberOfPlayers;
  }

  public void setNumberOfPlayers(int numberOfPlayers) {
    this.numberOfPlayers = numberOfPlayers;
  }

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public int getVolume() {
    return volume;
  }

  public void setVolume(int volume) {
    this.volume = volume;
  }

  public String getLaunchCustomVar() {
    return launchCustomVar;
  }

  public void setLaunchCustomVar(String launchCustomVar) {
    this.launchCustomVar = launchCustomVar;
  }

  public String getKeepDisplays() {
    return keepDisplays;
  }

  public void setKeepDisplays(String keepDisplays) {
    this.keepDisplays = keepDisplays;
  }

  public int getGameRating() {
    return gameRating;
  }

  public void setGameRating(int gameRating) {
    this.gameRating = gameRating;
  }

  public String getDof() {
    return dof;
  }

  public void setDof(String dof) {
    this.dof = dof;
  }

  public String getIPDBNum() {
    return IPDBNum;
  }

  public void setIPDBNum(String IPDBNum) {
    this.IPDBNum = IPDBNum;
  }

  public String getAltRunMode() {
    return altRunMode;
  }

  public void setAltRunMode(String altRunMode) {
    this.altRunMode = altRunMode;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getDesignedBy() {
    return designedBy;
  }

  public void setDesignedBy(String designedBy) {
    this.designedBy = designedBy;
  }
}
