package de.mephisto.vpin.restclient.representations;

import java.util.Date;

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
  private int id;
  private Date lastPlayed;
  private int numberPlays;
  private boolean directB2SAvailable;
  private boolean pupPackAvailable;
  private int validationState;
  private int volume;
  private String hsFileName;
  private boolean romExists;
  private String ignoredValidations;

  public String getIgnoredValidations() {
    return ignoredValidations;
  }

  public void setIgnoredValidations(String ignoredValidations) {
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

  public int getVolume() {
    return volume;
  }

  public void setVolume(int volume) {
    this.volume = volume;
  }

  public int getValidationState() {
    return validationState;
  }

  public void setValidationState(int validationState) {
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
