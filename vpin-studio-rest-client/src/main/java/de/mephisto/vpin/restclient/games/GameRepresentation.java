package de.mephisto.vpin.restclient.games;

import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.validation.ValidationState;

import java.util.Date;
import java.util.List;

/**
 * {
 * rom: "term3",
 * gameDisplayName: "Terminator 3",
 * gameFileName: "Terminator 3.vpx",
 * id: 48,
 * lastPlayed: null,
 * numberPlays: 0,
 * emulatorName: "Visual Pinball X",
 * },
 */
public class GameRepresentation {
  private String rom;
  private String romAlias;
  private String scannedRom;
  private String scannedAltRom;
  private int nvOffset;
  private String gameDisplayName;
  private String gameFileName;
  private Date dateAdded;
  private Date dateUpdated;
  private String gameName;
  private String version;
  private long gameFileSize;
  private String tableName;
  private boolean disabled;
  private boolean updateAvailable;
  private int id;
  private String comment;
  private int rating;
  private Date modified;
  private ValidationState validationState;
  private boolean hasMissingAssets;
  private boolean hasOtherIssues;
  private boolean validScoreConfiguration;

  private boolean played;
  private int gameStatus;

  private String hsFileName;
  private String highscoreIniFilename;
  private String scannedHsFileName;
  private boolean romExists;
  private List<Integer> ignoredValidations;
  private String highscoreType;
  private boolean selected;
  private boolean romRequired;
  private String assets;
  private boolean defaultBackgroundAvailable;
  private AltColorTypes altColorType;
  private String extTableId;
  private String extTableVersionId;
  private String extVersion;
  private String launcher;
  private int emulatorId;
  private String pupPackName;
  private Long templateId;
  private boolean vpxGame;
  private boolean fpGame;
  private boolean fxGame;

  private boolean cardDisabled;
  private boolean eventLogAvailable;

  private VPSChanges vpsUpdates = new VPSChanges();

  private String patchVersion;

  private String directB2SPath;
  private String gameFilePath;
  private String povPath;
  private String iniPath;
  private String resPath;
  private String pupPackPath;
  private boolean altSoundAvailable;

  private int nbDirectB2S = -1;

  public String getHighscoreIniFilename() {
    return highscoreIniFilename;
  }

  public void setHighscoreIniFilename(String highscoreIniFilename) {
    this.highscoreIniFilename = highscoreIniFilename;
  }

  public String getPatchVersion() {
    return patchVersion;
  }

  public void setPatchVersion(String patchVersion) {
    this.patchVersion = patchVersion;
  }

  public int getRating() {
    return rating;
  }

  public void setRating(int rating) {
    this.rating = rating;
  }

  public Date getDateUpdated() {
    return dateUpdated;
  }

  public void setDateUpdated(Date dateUpdated) {
    this.dateUpdated = dateUpdated;
  }

  public String getLauncher() {
    return launcher;
  }

  public void setLauncher(String launcher) {
    this.launcher = launcher;
  }

  public boolean isEventLogAvailable() {
    return eventLogAvailable;
  }

  public void setEventLogAvailable(boolean eventLogAvailable) {
    this.eventLogAvailable = eventLogAvailable;
  }

  public boolean isCardDisabled() {
    return cardDisabled;
  }

  public void setCardDisabled(boolean cardDisabled) {
    this.cardDisabled = cardDisabled;
  }

  public boolean isPlayed() {
    return played;
  }
  public void setPlayed(boolean played) {
    this.played = played;
  }

  public int getGameStatus() {
    return gameStatus;
  }
  public void setGameStatus(int gameStatus) {
    this.gameStatus = gameStatus;
  }

  public String getDirectB2SPath() {
    return directB2SPath;
  }

  public void setDirectB2SPath(String directB2SPath) {
    this.directB2SPath = directB2SPath;
  }

  public String getGameFilePath() {
    return gameFilePath;
  }

  public void setGameFilePath(String gameFilePath) {
    this.gameFilePath = gameFilePath;
  }

  public String getPovPath() {
    return povPath;
  }

  public void setPovPath(String povPath) {
    this.povPath = povPath;
  }

  public String getIniPath() {
    return iniPath;
  }

  public void setIniPath(String iniPath) {
    this.iniPath = iniPath;
  }

  public String getResPath() {
    return resPath;
  }

  public void setResPath(String resPath) {
    this.resPath = resPath;
  }

  public String getPupPackPath() {
    return pupPackPath;
  }

  public void setPupPackPath(String pupPackPath) {
    this.pupPackPath = pupPackPath;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public boolean isVpxGame() {
    return vpxGame;
  }

  public void setVpxGame(boolean vpxGame) {
    this.vpxGame = vpxGame;
  }

  public boolean isFpGame() {
    return fpGame;
  }

  public void setFpGame(boolean fpGame) {
    this.fpGame = fpGame;
  }

  public boolean isFxGame() {
    return fxGame;
  }

  public void setFxGame(boolean fxGame) {
    this.fxGame = fxGame;
  }

  public String getPupPackName() {
    return pupPackName;
  }

  public void setPupPackName(String pupPackName) {
    this.pupPackName = pupPackName;
  }

  public Long getTemplateId() {
    return templateId;
  }

  public void setTemplateId(Long templateId) {
    this.templateId = templateId;
  }

  public Date getDateAdded() {
    return dateAdded;
  }

  public void setDateAdded(Date dateAdded) {
    this.dateAdded = dateAdded;
  }

  public VPSChanges getVpsUpdates() {
    return vpsUpdates;
  }

  public void setVpsUpdates(VPSChanges updates) {
    this.vpsUpdates = updates;
  }

  public String getExtVersion() {
    return extVersion;
  }

  public void setExtVersion(String extVersion) {
    this.extVersion = extVersion;
  }

  public boolean isUpdateAvailable() {
    return updateAvailable;
  }

  public void setUpdateAvailable(boolean updateAvailable) {
    this.updateAvailable = updateAvailable;
  }

  public boolean isDisabled() {
    return disabled;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  public String getGameName() {
    return gameName;
  }

  public void setGameName(String gameName) {
    this.gameName = gameName;
  }

  public AltColorTypes getAltColorType() {
    return altColorType;
  }

  public void setAltColorType(AltColorTypes altColorType) {
    this.altColorType = altColorType;
  }

  public String getExtTableId() {
    return extTableId;
  }

  public void setExtTableId(String extTableId) {
    this.extTableId = extTableId;
  }

  public String getExtTableVersionId() {
    return extTableVersionId;
  }

  public void setExtTableVersionId(String extTableVersionId) {
    this.extTableVersionId = extTableVersionId;
  }

  public boolean isAltSoundAvailable() {
    return altSoundAvailable;
  }

  public void setAltSoundAvailable(boolean altSoundAvailable) {
    this.altSoundAvailable = altSoundAvailable;
  }

  public boolean isDefaultBackgroundAvailable() {
    return defaultBackgroundAvailable;
  }

  public void setDefaultBackgroundAvailable(boolean defaultBackgroundAvailable) {
    this.defaultBackgroundAvailable = defaultBackgroundAvailable;
  }

  public String getAssets() {
    return assets;
  }

  public void setAssets(String assets) {
    this.assets = assets;
  }

  public boolean isRomRequired() {
    return romRequired;
  }

  public void setRomRequired(boolean romRequired) {
    this.romRequired = romRequired;
  }

  public long getGameFileSize() {
    return gameFileSize;
  }

  public void setGameFileSize(long gameFileSize) {
    this.gameFileSize = gameFileSize;
  }

  public String getHighscoreType() {
    return highscoreType;
  }

  public void setHighscoreType(String highscoreType) {
    this.highscoreType = highscoreType;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public Date getModified() {
    return modified;
  }

  public void setModified(Date modified) {
    this.modified = modified;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public List<Integer> getIgnoredValidations() {
    return ignoredValidations;
  }

  public void setIgnoredValidations(List<Integer> ignoredValidations) {
    this.ignoredValidations = ignoredValidations;
  }

  public boolean isRomExists() {
    return romExists;
  }

  public void setRomExists(boolean romExists) {
    this.romExists = romExists;
  }

  public String getHsFileName() {
    return hsFileName;
  }

  public void setHsFileName(String hsFileName) {
    this.hsFileName = hsFileName;
  }

  public String getScannedHsFileName() {
    return scannedHsFileName;
  }

  public void setScannedHsFileName(String scannedHsFileName) {
    this.scannedHsFileName = scannedHsFileName;
  }

  public ValidationState getValidationState() {
    return validationState;
  }
  public void setValidationState(ValidationState validationState) {
    this.validationState = validationState;
  }

  public boolean isHasMissingAssets() {
    return hasMissingAssets;
  }
  public void setHasMissingAssets(boolean hasMissingAssets) {
    this.hasMissingAssets = hasMissingAssets;
  }

  public boolean isHasOtherIssues() {
    return hasOtherIssues;
  }
  public void setHasOtherIssues(boolean hasOtherIssues) {
    this.hasOtherIssues = hasOtherIssues;
  }

  public boolean isValidScoreConfiguration() {
    return validScoreConfiguration;
  }
  public void setValidScoreConfiguration(boolean validScoreConfiguration) {
    this.validScoreConfiguration = validScoreConfiguration;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public String getGameDisplayName() {
    return gameDisplayName;
  }

  public void setGameDisplayName(String gameDisplayName) {
    this.gameDisplayName = gameDisplayName;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getNvOffset() {
    return nvOffset;
  }

  public void setNvOffset(int nvOffset) {
    this.nvOffset = nvOffset;
  }

  public String getRomAlias() {
    return romAlias;
  }

  public void setRomAlias(String romAlias) {
    this.romAlias = romAlias;
  }

  public String getScannedRom() {
    return scannedRom;
  }

  public void setScannedRom(String scannedRom) {
    this.scannedRom = scannedRom;
  }

  public String getScannedAltRom() {
    return scannedAltRom;
  }

  public void setScannedAltRom(String scannedAltRom) {
    this.scannedAltRom = scannedAltRom;
  }

  public String getGameFileName() {
    return gameFileName;
  }

  public void setGameFileName(String gameFileName) {
    this.gameFileName = gameFileName;
  }

  public int getNbDirectB2S() {
    return nbDirectB2S;
  }

  public void setNbDirectB2S(int nbDirectB2S) {
    this.nbDirectB2S = nbDirectB2S;
  }

  @Override
  public String toString() {
    return this.getGameDisplayName();
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof GameRepresentation) && ((GameRepresentation) obj).getId() == this.getId();
  }
}
