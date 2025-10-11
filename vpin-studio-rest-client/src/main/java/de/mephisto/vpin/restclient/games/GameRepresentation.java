package de.mephisto.vpin.restclient.games;

import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.cards.CardTemplateType;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.validation.ValidationState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
  private List<CompetitionType> competitionTypes = new ArrayList<>();

  private String hsFileName;
  private String highscoreIniFilename;
  private String scannedHsFileName;
  private boolean romExists;
  private List<Integer> ignoredValidations;
  private HighscoreType highscoreType;
  private boolean selected;
  private boolean romRequired;
  private String assets;
  private AltColorTypes altColorType;
  private String extTableId;
  private String extTableVersionId;
  private String extVersion;
  private String launcher;
  private int emulatorId;
  private String pupPackName;

  private Long highscoreCardTemplateId;
  private Long instructionCardTemplateId;
  private Long wheelTemplateId;

  private boolean cardDisabled;
  private boolean eventLogAvailable;
  private boolean ignoreUpdates;

  private VPSChanges vpsUpdates = new VPSChanges();

  private String patchVersion;

  private String directB2SPath;
  private String gameFilePath;
  private String povPath;
  private String iniPath;
  private String resPath;
  private boolean altSoundAvailable;

  private List<String> tags = new ArrayList<>();

  private int nbDirectB2S = -1;

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public boolean isIgnoreUpdates() {
    return ignoreUpdates;
  }

  public void setIgnoreUpdates(boolean ignoreUpdates) {
    this.ignoreUpdates = ignoreUpdates;
  }

  public List<CompetitionType> getCompetitionTypes() {
    return competitionTypes;
  }

  public void setCompetitionTypes(List<CompetitionType> competitionTypes) {
    this.competitionTypes = competitionTypes;
  }

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

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getPupPackName() {
    return pupPackName;
  }

  public void setPupPackName(String pupPackName) {
    this.pupPackName = pupPackName;
  }

  @JsonIgnore
  public Long getTemplateId(CardTemplateType templateType) {
    switch (templateType) {
      case HIGSCORE_CARD:
        return getHighscoreCardTemplateId();
      case INSTRUCTIONS_CARD:
        return getInstructionCardTemplateId();
      case WHEEL:
        return getWheelTemplateId();
    }
    return null;
  }

  public Long getHighscoreCardTemplateId() {
    return highscoreCardTemplateId;
  }

  public void setHighscoreCardTemplateId(Long highscoreCardTemplateId) {
    this.highscoreCardTemplateId = highscoreCardTemplateId;
  }

  public Long getInstructionCardTemplateId() {
    return instructionCardTemplateId;
  }

  public void setInstructionCardTemplateId(Long instructionCardTemplateId) {
    this.instructionCardTemplateId = instructionCardTemplateId;
  }

  public Long getWheelTemplateId() {
    return wheelTemplateId;
  }

  public void setWheelTemplateId(Long wheelTemplateId) {
    this.wheelTemplateId = wheelTemplateId;
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

  public HighscoreType getHighscoreType() {
    return highscoreType;
  }

  public void setHighscoreType(HighscoreType highscoreType) {
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
