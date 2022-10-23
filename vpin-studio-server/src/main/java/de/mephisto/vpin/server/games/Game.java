package de.mephisto.vpin.server.games;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.server.popper.Emulator;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Date;

public class Game {

  private String rom;
  private String originalRom;
  private String gameDisplayName;
  private String gameFileName;
  private int id;
  private int nvOffset;
  private String hsFileName;
  private Emulator emulator;

  private File gameFile;
  private File romFile;

  private Date lastPlayed;
  private int numberPlays;
  private int validationState;
  private String ignoredValidations;
  private int volume;
  private String rawHighscore;

  private SystemService systemService;

  public Game() {

  }

  public Game(@NonNull SystemService systemService) {
    this.systemService = systemService;
  }

  @SuppressWarnings("unused")
  public boolean hasHighscore() {
    if (this.getNvRamFile() != null && this.getNvRamFile().exists()) {
      return true;
    }

    if (this.getVPRegFolder() != null && this.getVPRegFolder().exists()) {
      return true;
    }

    if (this.getEMHighscoreFile() != null && this.getEMHighscoreFile().exists()) {
      return true;
    }
    return false;
  }

  @Nullable
  @JsonIgnore
  public File getVPRegFolder() {
    if (!StringUtils.isEmpty(this.getRom())) {
      return new File(systemService.getExtractedVPRegFolder(), getRom());
    }
    return null;
  }

  @NonNull
  public Emulator getEmulator() {
    return emulator;
  }

  public void setEmulator(@NonNull Emulator emulator) {
    this.emulator = emulator;
  }

  @SuppressWarnings("unused")
  public int getNumberPlays() {
    return numberPlays;
  }

  public void setNumberPlays(int numberPlays) {
    this.numberPlays = numberPlays;
  }

  @SuppressWarnings("unused")
  @Nullable
  public Date getLastPlayed() {
    return lastPlayed;
  }

  public void setLastPlayed(Date lastPlayed) {
    this.lastPlayed = lastPlayed;
  }

  public int getVolume() {
    return volume;
  }

  public void setVolume(int volume) {
    this.volume = volume;
  }

  public String getIgnoredValidations() {
    return ignoredValidations;
  }

  public void setIgnoredValidations(String ignoredValidations) {
    this.ignoredValidations = ignoredValidations;
  }

  public SystemService getSystemService() {
    return systemService;
  }

  public void setSystemService(SystemService systemService) {
    this.systemService = systemService;
  }

  @Nullable
  @JsonIgnore
  public File getNvRamFile() {
    File nvRamFolder = new File(systemService.getMameFolder(), "nvram");

    String originalRom = getOriginalRom() != null ? this.getOriginalRom() : this.getRom();
    File defaultNVFile = new File(nvRamFolder, originalRom + ".nv");
    if (this.getNvOffset() == 0) {
      return defaultNVFile;
    }

    //if the text file exists, the current nv file contains the highscore of this table
    File versionTextFile = new File(systemService.getMameFolder(), this.getRom() + " v" + getNvOffset() + ".txt");
    if (versionTextFile.exists()) {
      return defaultNVFile;
    }

    //else, we can check if a nv file with the alias and version exists
    File versionNVAliasedFile = new File(systemService.getMameFolder(), originalRom + " v" + getNvOffset() + ".nv");
    if (versionNVAliasedFile.exists()) {
      return versionNVAliasedFile;
    }

    return defaultNVFile;
  }

  @Nullable
  public File getEMHighscoreFile() {
    if (!StringUtils.isEmpty(this.getHsFileName())) {
      return new File(systemService.getVisualPinballUserFolder(), this.getHsFileName());
    }
    return null;
  }

  @NonNull
  @JsonIgnore
  public File getGameFile() {
    return gameFile;
  }

  public void setGameFile(@NonNull File gameFile) {
    this.gameFile = gameFile;
  }

  public String getRom() {
    return rom;
  }

  public boolean isDirectB2SAvailable() {
    String name = FilenameUtils.getBaseName(this.getGameFileName());
    String directB2SName = name + ".directb2s";
    return new File(systemService.getVPXTablesFolder(), directB2SName).exists();
  }

  public boolean isPupPackAvailable() {
    if (StringUtils.isEmpty(this.getRom())) {
      return false;
    }

    File pupVideos = new File(systemService.getPinUPSystemFolder(), "PUPVideos");
    File pupPackFolder = new File(pupVideos, getRom());
    return pupPackFolder.exists() && pupPackFolder.listFiles().length > 1;
  }

  public String getRawHighscore() {
    return rawHighscore;
  }

  public void setRawHighscore(String rawHighscore) {
    this.rawHighscore = rawHighscore;
  }

  public String getHsFileName() {
    return hsFileName;
  }

  public void setHsFileName(String hsFileName) {
    this.hsFileName = hsFileName;
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

  public String getGameFileName() {
    return gameFileName;
  }

  public void setGameFileName(String gameFileName) {
    this.gameFileName = gameFileName;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getOriginalRom() {
    return originalRom;
  }

  public void setOriginalRom(String originalRom) {
    this.originalRom = originalRom;
  }

  public int getNvOffset() {
    return nvOffset;
  }

  public void setNvOffset(int nvOffset) {
    this.nvOffset = nvOffset;
  }

  public int getValidationState() {
    return validationState;
  }

  public void setValidationState(int validationState) {
    this.validationState = validationState;
  }

  @SuppressWarnings("unused")
  @Nullable
  @JsonIgnore
  public File getRomFile() {
    return romFile;
  }

  public void setRomFile(File romFile) {
    this.romFile = romFile;
  }

  @NonNull
  @JsonIgnore
  public File getDirectB2SFile() {
    String baseName = FilenameUtils.getBaseName(this.getGameFileName());
    return new File(systemService.getVPXTablesFolder(), baseName + ".directb2s");
  }

  @NonNull
  @JsonIgnore
  public File getDirectB2SBackgroundImage() {
    String targetName = FilenameUtils.getBaseName(getGameFileName()) + ".png";
    return new File(systemService.getB2SImageExtractionFolder(), targetName);
  }

  @Override
  public String toString() {
    return this.getGameDisplayName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Game game = (Game) o;

    return id == game.id;
  }

  @Override
  public int hashCode() {
    return id;
  }
}
