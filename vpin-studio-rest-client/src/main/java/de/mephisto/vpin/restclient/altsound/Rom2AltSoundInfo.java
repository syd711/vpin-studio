package de.mephisto.vpin.restclient.altsound;

import java.util.ArrayList;
import java.util.List;

/**
 * What rom2altsound knows about the ROM of a game: whether an ALT sound pack can be generated from it.
 */
public class Rom2AltSoundInfo {
  /**
   * false when the rom2altsound executable is not installed or the OS is not supported
   */
  private boolean available;
  private String rom;
  private String romFile;
  private boolean romFileExists;
  /**
   * the folder the generated pack is installed to
   */
  private String altSoundFolder;
  private boolean altSoundExists;

  /**
   * the check status of the ROM zip: ok, incomplete, bad, …
   */
  private String status;
  private String set;
  private String parent;
  private String description;
  private String manufacturer;
  private String year;
  /**
   * PinMAME's SNDBRD_* type, e.g. SNDBRD_DCS95
   */
  private String soundBoard;
  private List<String> soundInterfaces = new ArrayList<>();
  private boolean soundRomsGood;
  private String soundRomId;
  private List<String> issues = new ArrayList<>();

  /**
   * true when a pack can be generated: the ROM zip exists, it was identified and its sound ROMs are good
   */
  private boolean supported;
  private String error;

  public boolean isAvailable() {
    return available;
  }

  public void setAvailable(boolean available) {
    this.available = available;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public String getRomFile() {
    return romFile;
  }

  public void setRomFile(String romFile) {
    this.romFile = romFile;
  }

  public boolean isRomFileExists() {
    return romFileExists;
  }

  public void setRomFileExists(boolean romFileExists) {
    this.romFileExists = romFileExists;
  }

  public String getAltSoundFolder() {
    return altSoundFolder;
  }

  public void setAltSoundFolder(String altSoundFolder) {
    this.altSoundFolder = altSoundFolder;
  }

  public boolean isAltSoundExists() {
    return altSoundExists;
  }

  public void setAltSoundExists(boolean altSoundExists) {
    this.altSoundExists = altSoundExists;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getSet() {
    return set;
  }

  public void setSet(String set) {
    this.set = set;
  }

  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public String getSoundBoard() {
    return soundBoard;
  }

  public void setSoundBoard(String soundBoard) {
    this.soundBoard = soundBoard;
  }

  public List<String> getSoundInterfaces() {
    return soundInterfaces;
  }

  public void setSoundInterfaces(List<String> soundInterfaces) {
    this.soundInterfaces = soundInterfaces;
  }

  public boolean isSoundRomsGood() {
    return soundRomsGood;
  }

  public void setSoundRomsGood(boolean soundRomsGood) {
    this.soundRomsGood = soundRomsGood;
  }

  public String getSoundRomId() {
    return soundRomId;
  }

  public void setSoundRomId(String soundRomId) {
    this.soundRomId = soundRomId;
  }

  public List<String> getIssues() {
    return issues;
  }

  public void setIssues(List<String> issues) {
    this.issues = issues;
  }

  public boolean isSupported() {
    return supported;
  }

  public void setSupported(boolean supported) {
    this.supported = supported;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
