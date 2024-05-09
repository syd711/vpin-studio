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
  private int nvOffset;
  private String gameDisplayName;
  private String gameFileName;
  private Date dateAdded;
  private String gameName;
  private String version;
  private long gameFileSize;
  private String tableName;
  private boolean disabled;
  private boolean updateAvailable;
  private int id;
  private String notes;
  private Date modified;
  private GameMediaRepresentation gameMedia;
  private boolean directB2SAvailable;
  private boolean gameFileAvailable;
  private ValidationState validationState;
  private String hsFileName;
  private boolean romExists;
  private List<Integer> ignoredValidations;
  private String highscoreType;
  private boolean povAvailable;
  private boolean iniAvailable;
  private boolean selected;
  private boolean romRequired;
  private String assets;
  private boolean defaultBackgroundAvailable;
  private boolean altSoundAvailable;
  private AltColorTypes altColorType;
  private String extTableId;
  private String extTableVersionId;
  private String extVersion;
  private int emulatorId;
  private String pupPackName;
  private Long templateId;
  private boolean pupPackAvailable;
  private boolean vpxGame;
  private VPSChanges vpsUpdates = new VPSChanges();

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public boolean isVpxGame() {
    return vpxGame;
  }

  public void setVpxGame(boolean vpxGame) {
    this.vpxGame = vpxGame;
  }

  public String getPupPackName() {
    return pupPackName;
  }

  public boolean isPupPackAvailable() {
    return pupPackAvailable;
  }

  public void setPupPackAvailable(boolean pupPackAvailable) {
    this.pupPackAvailable = pupPackAvailable;
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

  public void setUpdates(VPSChanges updates) {
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

  public boolean isGameFileAvailable() {
    return gameFileAvailable;
  }

  public void setGameFileAvailable(boolean gameFileAvailable) {
    this.gameFileAvailable = gameFileAvailable;
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

  public boolean isPovAvailable() {
    return povAvailable;
  }

  public void setPovAvailable(boolean povAvailable) {
    this.povAvailable = povAvailable;
  }

  public boolean isIniAvailable() {
    return iniAvailable;
  }

  public void setIniAvailable(boolean iniAvailable) {
    this.iniAvailable = iniAvailable;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public GameMediaRepresentation getGameMedia() {
    return gameMedia;
  }

  public void setGameMedia(GameMediaRepresentation gameMedia) {
    this.gameMedia = gameMedia;
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

  public ValidationState getValidationState() {
    return validationState;
  }

  public void setValidationState(ValidationState validationState) {
    this.validationState = validationState;
  }

  public boolean isDirectB2SAvailable() {
    return directB2SAvailable;
  }

  public void setDirectB2SAvailable(boolean directB2SAvailable) {
    this.directB2SAvailable = directB2SAvailable;
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

  public String getGameFileName() {
    return gameFileName;
  }

  public void setGameFileName(String gameFileName) {
    this.gameFileName = gameFileName;
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
