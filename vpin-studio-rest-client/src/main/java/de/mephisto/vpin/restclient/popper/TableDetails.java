package de.mephisto.vpin.restclient.popper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TableDetails {
  public final static String ARCHIVE_FILENAME = "table-details.json";

  private String emulatorType;
  private String gameName;
  private String gameFileName;
  private String gameDisplayName;
  private GameType gameType;
  private String fileVersion;
  private Date dateAdded;
  private String gameTheme;
  private String notes;
  private Integer gameYear;
  private String romName;
  private String manufacturer;
  private Integer numberOfPlayers;
  private String tags;
  private String category;
  private String author;
  private String volume;
  private String launchCustomVar;
  private String keepDisplays;
  private Integer gameRating;
  private String dof;
  private String IPDBNum;
  private String altRunMode;
  private String url;
  private String designedBy;
  private String altLaunchExe;
  private List<String> launcherList = new ArrayList<>();

  public String getAltLaunchExe() {
    return altLaunchExe;
  }

  public void setAltLaunchExe(String altLaunchExe) {
    this.altLaunchExe = altLaunchExe;
  }

  public List<String> getLauncherList() {
    return launcherList;
  }

  public void setLauncherList(List<String> launcherList) {
    this.launcherList = launcherList;
  }

  public GameType getGameType() {
    return gameType;
  }

  public void setGameType(GameType gameType) {
    this.gameType = gameType;
  }

  public String getFileVersion() {
    return fileVersion;
  }

  public void setFileVersion(String fileVersion) {
    this.fileVersion = fileVersion;
  }

  public Integer getGameYear() {
    return gameYear;
  }

  public void setGameYear(Integer gameYear) {
    this.gameYear = gameYear;
  }

  public Integer getNumberOfPlayers() {
    return numberOfPlayers;
  }

  public void setNumberOfPlayers(Integer numberOfPlayers) {
    this.numberOfPlayers = numberOfPlayers;
  }

  public Integer getGameRating() {
    return gameRating;
  }

  public void setGameRating(Integer gameRating) {
    this.gameRating = gameRating;
  }

  public Date getDateAdded() {
    return dateAdded;
  }

  public void setDateAdded(Date dateAdded) {
    this.dateAdded = dateAdded;
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

  public String getRomName() {
    return romName;
  }

  public void setRomName(String romName) {
    this.romName = romName;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
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

  public String getVolume() {
    return volume;
  }

  public void setVolume(String volume) {
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
