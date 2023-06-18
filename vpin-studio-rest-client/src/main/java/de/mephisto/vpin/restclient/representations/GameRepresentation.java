package de.mephisto.vpin.restclient.representations;

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
  private String originalRom;
  private int nvOffset;
  private String gameDisplayName;
  private String gameFileName;
  private long gameFileSize;
  private String tableName;
  private int id;
  private Date modified;
  private Date lastPlayed;
  private int numberPlays;
  private GameMediaRepresentation gameMedia;
  private EmulatorRepresentation emulator;
  private boolean directB2SAvailable;
  private boolean gameFileAvailable;
  private boolean pupPackAvailable;
  private ValidationState validationState;
  private String hsFileName;
  private boolean romExists;
  private List<Integer> ignoredValidations;
  private String highscoreType;
  private boolean povAvailable;
  private boolean selected;
  private boolean romRequired;
  private String assets;
  private boolean defaultBackgroundAvailable;
  private boolean altSoundAvailable;
  private boolean altColorAvailable;
  private String extTableId;
  private String extTableVersionId;

  public boolean isAltColorAvailable() {
    return altColorAvailable;
  }

  public void setAltColorAvailable(boolean altColorAvailable) {
    this.altColorAvailable = altColorAvailable;
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

  public EmulatorRepresentation getEmulator() {
    return emulator;
  }

  public void setEmulator(EmulatorRepresentation emulator) {
    this.emulator = emulator;
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

  public boolean isPupPackAvailable() {
    return pupPackAvailable;
  }

  public void setPupPackAvailable(boolean pupPackAvailable) {
    this.pupPackAvailable = pupPackAvailable;
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

  public Date getLastPlayed() {
    return lastPlayed;
  }

  public void setLastPlayed(Date lastPlayed) {
    this.lastPlayed = lastPlayed;
  }

  public int getNumberPlays() {
    return numberPlays;
  }

  public void setNumberPlays(int numberPlays) {
    this.numberPlays = numberPlays;
  }

  public int getNvOffset() {
    return nvOffset;
  }

  public void setNvOffset(int nvOffset) {
    this.nvOffset = nvOffset;
  }

  public String getOriginalRom() {
    return originalRom;
  }

  public void setOriginalRom(String originalRom) {
    this.originalRom = originalRom;
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
