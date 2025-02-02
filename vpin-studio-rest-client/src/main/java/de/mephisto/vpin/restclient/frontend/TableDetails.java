package de.mephisto.vpin.restclient.frontend;

import javafx.scene.control.Tooltip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TableDetails {
  public final static String ARCHIVE_FILENAME = "table-details.json";
  private final static Logger LOG = LoggerFactory.getLogger(TableDetails.class);

  private int sqlVersion;
  private int emulatorId;

  private int status;
  private String gameName;
  private String gameFileName;
  private String gameDisplayName;
  private String gameType;
  private String gameVersion;
  private Date dateAdded;
  private Date dateModified;
  private String gameTheme;
  private String notes;
  private Integer gameYear;
  private String romName;
  private String manufacturer;
  private Integer numberOfPlayers;
  private Date lastPlayed;
  private Integer numberPlays;
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
  private String custom2;
  private String custom3;
  private String special;
  private String mediaSearch;

  //1.5 values
  private String custom4;
  private String custom5;
  private String webGameId;
  private String romAlt;
  private String webLink2Url;
  private String tourneyId;
  private boolean mod;

  //Game Extra
  private String gDetails;
  private String gNotes;
  private String gLog;
  private String gPlayLog;

  // mapped values
  private String hsFilename;


  public String getSpecial() {
    return special;
  }

  public void setSpecial(String special) {
    this.special = special;
  }

  public String getMediaSearch() {
    return mediaSearch;
  }

  public void setMediaSearch(String mediaSearch) {
    this.mediaSearch = mediaSearch;
  }

  public boolean isMod() {
    return mod;
  }

  public void setMod(boolean mod) {
    this.mod = mod;
  }

  public boolean isPopper15() {
    return sqlVersion >= 64;
  }

  public int getSqlVersion() {
    return sqlVersion;
  }

  public void setSqlVersion(int sqlVersion) {
    this.sqlVersion = sqlVersion;
  }

  public String getgPlayLog() {
    return gPlayLog;
  }

  public void setgPlayLog(String gPlayLog) {
    this.gPlayLog = gPlayLog;
  }

  public String getCustom2() {
    return custom2;
  }

  public void setCustom2(String custom2) {
    this.custom2 = custom2;
  }

  public String getCustom3() {
    return custom3;
  }

  public void setCustom3(String custom3) {
    this.custom3 = custom3;
  }

  public String getCustom4() {
    return custom4;
  }

  public void setCustom4(String custom4) {
    this.custom4 = custom4;
  }

  public String getCustom5() {
    return custom5;
  }

  public void setCustom5(String custom5) {
    this.custom5 = custom5;
  }

  public String getWebGameId() {
    return webGameId;
  }

  public void setWebGameId(String webGameId) {
    this.webGameId = webGameId;
  }

  public String getRomAlt() {
    return romAlt;
  }

  public void setRomAlt(String romAlt) {
    this.romAlt = romAlt;
  }

  public String getWebLink2Url() {
    return webLink2Url;
  }

  public void setWebLink2Url(String webLink2Url) {
    this.webLink2Url = webLink2Url;
  }

  public String getTourneyId() {
    return tourneyId;
  }

  public void setTourneyId(String tourneyId) {
    this.tourneyId = tourneyId;
  }

  public String getgDetails() {
    return gDetails;
  }

  public void setgDetails(String gDetails) {
    this.gDetails = gDetails;
  }

  public String getgNotes() {
    return gNotes;
  }

  public void setgNotes(String gNotes) {
    this.gNotes = gNotes;
  }

  public String getgLog() {
    return gLog;
  }

  public void setgLog(String gLog) {
    this.gLog = gLog;
  }

  private List<String> launcherList = new ArrayList<>();

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  public Date getLastPlayed() {
    return lastPlayed;
  }

  public void setLastPlayed(Date lastPlayed) {
    this.lastPlayed = lastPlayed;
  }

  public Integer getNumberPlays() {
    return numberPlays;
  }

  public void setNumberPlays(Integer numberPlays) {
    this.numberPlays = numberPlays;
  }

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

  public String getGameType() {
    return gameType;
  }

  public void setGameType(String gameType) {
    this.gameType = gameType;
  }

  public String getGameVersion() {
    return gameVersion;
  }

  public void setGameVersion(String gameVersion) {
    this.gameVersion = gameVersion;
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

  public Date getDateModified() {
    return dateModified;
  }

  public void setDateModified(Date dateModified) {
    this.dateModified = dateModified;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
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

  public String getHsFilename() {
    return hsFilename;
  }

  public void setHsFilename(String hsFilename) {
    this.hsFilename = hsFilename;
  }
}
