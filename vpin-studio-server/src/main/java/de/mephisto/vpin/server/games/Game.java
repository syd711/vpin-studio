package de.mephisto.vpin.server.games;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.competitions.CompetitionType;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.puppack.PupPack;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Game {

  private String rom;
  private String romAlias;
  private String scannedRom;
  private String scannedAltRom;

  private String gameDisplayName;
  private String gameFileName;
  private String gameName;
  private String tableName;
  private String version;
  private boolean disabled;
  private boolean updateAvailable;
  private Date dateAdded;
  private Date dateUpdated;
  private int id;
  private int nvOffset;
  private String hsFileName;
  private String scannedHsFileName;
  private boolean cardDisabled;
  private String patchVersion;

  private int gameStatus;

  private GameEmulator emulator;
  private int emulatorId;

  private File gameFile;

  private File wheelImageFile;

  private ValidationState validationState;
  private boolean hasMissingAssets;
  private boolean hasOtherIssues;
  private boolean validScoreConfiguration;

  private List<Integer> ignoredValidations = new ArrayList<>();
  private HighscoreType highscoreType;
  private boolean altSoundAvailable;
  private AltColorTypes altColorType;

  private List<CompetitionType> competitionTypes = new ArrayList<>();

  private int nbDirectB2S;

  private boolean defaultBackgroundAvailable;
  private boolean eventLogAvailable;
  private PupPack pupPack;
  private List<Integer> playlists = new ArrayList<>();

  private String pupPackName;
  private Long templateId;
  private String extTableId;
  private String extTableVersionId;
  private String extVersion;
  private String comment;
  private String launcher;
  private Long numberPlayed;

  //internal value not exposed
  private String altLauncherExe;
  private boolean pupPackDisabled;

  private VPSChanges vpsChanges = new VPSChanges();

  private boolean foundControllerStop = false;
  private boolean foundTableExit = false;
  private boolean vrRoomSupport = false;
  private boolean vrRoomEnabled = false;

  private String dmdType;
  private String dmdGameName;
  private String dmdProjectFolder;

  private int rating = 0;

  public Game() {
  }

  public List<CompetitionType> getCompetitionTypes() {
    return competitionTypes;
  }

  public void setCompetitionTypes(List<CompetitionType> competitionTypes) {
    this.competitionTypes = competitionTypes;
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

  public boolean isVrRoomSupport() {
    return vrRoomSupport;
  }

  public void setVrRoomSupport(boolean vrRoomSupport) {
    this.vrRoomSupport = vrRoomSupport;
  }

  public boolean isVrRoomEnabled() {
    return vrRoomEnabled;
  }

  public void setVrRoomEnabled(boolean vrRoomEnabled) {
    this.vrRoomEnabled = vrRoomEnabled;
  }

  public boolean isPlayed() {
    return numberPlayed != null && numberPlayed > 0;
  }

  @JsonIgnore
  public boolean isPupPackDisabled() {
    return pupPackDisabled;
  }

  public void setPupPackDisabled(boolean pupPackDisabled) {
    this.pupPackDisabled = pupPackDisabled;
  }

  @JsonIgnore
  public long getNumberPlayed() {
    return numberPlayed;
  }

  public void setNumberPlayed(long numberPlayed) {
    this.numberPlayed = numberPlayed;
  }

  @JsonIgnore
  public String getAltLauncherExe() {
    return altLauncherExe;
  }

  public void setAltLauncherExe(String altLauncherExe) {
    this.altLauncherExe = altLauncherExe;
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

  public String getPupPackName() {
    if (this.pupPack != null) {
      return this.pupPack.getName();
    }
    return pupPackName;
  }

  public boolean isDefaultBackgroundAvailable() {
    return defaultBackgroundAvailable;
  }

  public void setDefaultBackgroundAvailable(boolean defaultBackgroundAvailable) {
    this.defaultBackgroundAvailable = defaultBackgroundAvailable;
  }

  public Date getDateUpdated() {
    return dateUpdated;
  }

  public void setDateUpdated(Date dateUpdated) {
    this.dateUpdated = dateUpdated;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public boolean isVpxGame() {
    return this.emulator != null && this.emulator.isVpxEmulator();
  }

  public boolean isFpGame() {
    return this.emulator.isFpEmulator();
  }

  public boolean isFxGame() {
    return this.emulator.isFxEmulator();
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

  public String getGameName() {
    return gameName;
  }

  public void setGameName(String gameName) {
    this.gameName = gameName;
  }

  public List<Integer> getPlaylists() {
    return playlists;
  }

  public void setPlaylists(List<Integer> playlists) {
    this.playlists = playlists;
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

  public boolean isDisabled() {
    return disabled;
  }

  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  public VPSChanges getVpsUpdates() {
    return vpsChanges;
  }

  public void setVpsUpdates(VPSChanges vpsChanges) {
    if (vpsChanges != null) {
      this.vpsChanges = vpsChanges;
    }
  }

  public boolean isFoundControllerStop() {
    return foundControllerStop;
  }

  public void setFoundControllerStop(boolean foundControllerStop) {
    this.foundControllerStop = foundControllerStop;
  }

  public boolean isFoundTableExit() {
    return foundTableExit;
  }

  public void setFoundTableExit(boolean foundTableExit) {
    this.foundTableExit = foundTableExit;
  }

  @JsonIgnore
  public File getWheelImage() {
    return wheelImageFile;
  }

  public void setWheelImage(File wheelFile) {
    this.wheelImageFile = wheelFile;
  }

   /*
  @JsonIgnore
  public Image getWheelImage() {
    FrontendMediaItem frontendMediaItem = getGameMedia().getDefaultMediaItem(VPinScreen.Wheel);
    Image image = null;
    if (frontendMediaItem != null) {
      try {
        BufferedImage bufferedImage = ImageUtil.loadImage(frontendMediaItem.getFile());
        image = SwingFXUtils.toFXImage(bufferedImage, null);
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return image;
  }
  */

  @JsonIgnore
  @Nullable
  public PupPack getPupPack() {
    return pupPack;
  }

  public void setPupPack(PupPack pupPack) {
    this.pupPack = pupPack;
  }

  public String getPupPackPath() {
    if (pupPack != null && pupPack.getPupPackFolder().exists()) {
      return pupPack.getPupPackFolder().getAbsolutePath();
    }
    return null;
  }

  public long getGameFileSize() {
    if (this.getGameFile().exists()) {
      return this.getGameFile().length();
    }
    return -1;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public HighscoreType getHighscoreType() {
    return highscoreType;
  }

  public void setHighscoreType(HighscoreType highscoreType) {
    this.highscoreType = highscoreType;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public int getGameStatus() {
    return gameStatus;
  }

  public void setGameStatus(int gameStatus) {
    this.gameStatus = gameStatus;
  }

  @Nullable
  @JsonIgnore
  public GameEmulator getEmulator() {
    return emulator;
  }

  public void setEmulator(@NonNull GameEmulator emulator) {
    this.emulator = emulator;
    this.emulatorId = emulator.getId();
  }

  public int getEmulatorId() {
    return this.emulatorId;
  }

  public void setEmulatorId(int emuId) {
    this.emulatorId = emuId;
  }

  public List<Integer> getIgnoredValidations() {
    return ignoredValidations;
  }

  public void setIgnoredValidations(List<Integer> ignoredValidations) {
    this.ignoredValidations = ignoredValidations;
  }

  @JsonIgnore
  @Nullable
  public File getHighscoreFile() {
    HighscoreType highscoreType = getHighscoreType();
    if (highscoreType != null) {
      switch (highscoreType) {
        case EM: {
          return getHighscoreTextFile();
        }
        case VPReg: {
          return getEmulator().getVPRegFile();
        }
        case NVRam: {
          return getNvRamFile();
        }
      }
    }
    return null;
  }

  @JsonIgnore
  @Nullable
  public File getHighscoreTextFile() {
    if (!StringUtils.isEmpty(this.getHsFileName())) {
      return new File(emulator.getUserFolder(), this.getHsFileName());
    }
    return null;
  }

  @JsonIgnore
  @Nullable
  public File getAlternateHighscoreTextFile(@NonNull String name) {
    if (!StringUtils.isEmpty(name)) {
      if (!name.endsWith(".txt")) {
        name = name + ".txt";
      }
      return new File(emulator.getUserFolder(), name);
    }
    return null;
  }

  @NonNull
  @JsonIgnore
  public File getPOVFile() {
    return new File(getGameFile().getParentFile(), FilenameUtils.getBaseName(gameFileName) + ".pov");
  }


  @NonNull
  @JsonIgnore
  public File getIniFile() {
    return new File(getGameFile().getParentFile(), FilenameUtils.getBaseName(gameFileName) + ".ini");
  }

  @Nullable
  @JsonIgnore
  public File getHighscoreIniFile() {
    File iniFile = null;
    if (!StringUtils.isEmpty(getRom())) {
      String iniScoreName = FilenameUtils.getBaseName(getRom()) + "_glf.ini";
      iniFile = new File(getGameFile().getParentFile(), iniScoreName);
    }

    if (iniFile == null || !iniFile.exists()) {
      if (!StringUtils.isEmpty(getTableName())) {
        String iniScoreName = FilenameUtils.getBaseName(getTableName()) + "_glf.ini";
        iniFile = new File(getGameFile().getParentFile(), iniScoreName);
      }
    }
    return iniFile;
  }

  public String getHighscoreIniFilename() {
    File iniFile = getHighscoreIniFile();
    if (iniFile != null && iniFile.exists()) {
      return iniFile.getAbsolutePath();
    }
    return null;
  }

  @NonNull
  @JsonIgnore
  public File getVBSFile() {
    return new File(getGameFile().getParentFile(), FilenameUtils.getBaseName(gameFileName) + ".vbs");
  }

  @NonNull
  @JsonIgnore
  public File getResFile() {
    return new File(getGameFile().getParentFile(), FilenameUtils.getBaseName(gameFileName) + ".res");
  }

  @NonNull
  @JsonIgnore
  public File getGameFile() {
    return gameFile;
  }

  public boolean isRomRequired() {
    return getHighscoreType() != null && HighscoreType.NVRam.equals(getHighscoreType());
  }

  public Date getModified() {
    if (this.gameFile != null && this.gameFile.lastModified() > 0) {
      return new Date(this.gameFile.lastModified());
    }
    return null;
  }

  public String getPovPath() {
    if (this.getPOVFile().exists()) {
      return this.getPOVFile().getAbsolutePath();
    }
    return null;
  }

  public String getIniPath() {
    if (this.getIniFile().exists()) {
      return this.getIniFile().getAbsolutePath();
    }
    return null;
  }

  public String getResPath() {
    if (this.getResFile().exists()) {
      return this.getResFile().getAbsolutePath();
    }
    return null;
  }

  public void setGameFile(@NonNull File gameFile) {
    this.gameFile = gameFile;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public String getDirectB2SPath() {
    if (getDirectB2SFile().exists()) {
      return getDirectB2SFile().getAbsolutePath();
    }
    return null;
  }

  public String getGameFilePath() {
    if (this.getGameFile().exists()) {
      return this.getGameFile().getAbsolutePath();
    }
    return null;
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

  public int getNvOffset() {
    return nvOffset;
  }

  public void setNvOffset(int nvOffset) {
    this.nvOffset = nvOffset;
  }

  public String getDMDType() {
    return dmdType;
  }

  public void setDMDType(String dmdType) {
    this.dmdType = dmdType;
  }

  public String getDMDGameName() {
    return dmdGameName;
  }

  public void setDMDGameName(String dmdGameName) {
    this.dmdGameName = dmdGameName;
  }

  public String getDMDProjectFolder() {
    return dmdProjectFolder;
  }

  public void setDMDProjectFolder(String dmdProjectFolder) {
    this.dmdProjectFolder = dmdProjectFolder;
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

  @Nullable
  @JsonIgnore
  public File getRomFile() {
    if (!StringUtils.isEmpty(this.getRom())) {
      return new File(emulator.getRomFolder(), this.getRom() + ".zip");
    }
    return null;
  }

  public AltColorTypes getAltColorType() {
    return altColorType;
  }

  public void setAltColorType(AltColorTypes altColorType) {
    this.altColorType = altColorType;
  }

  public void setAltSoundAvailable(boolean altSoundAvailable) {
    this.altSoundAvailable = altSoundAvailable;
  }

  public boolean isAltSoundAvailable() {
    return this.altSoundAvailable;
  }

  @Nullable
  @JsonIgnore
  public File getAltSoundFolder() {
    if (!StringUtils.isEmpty(this.getRomAlias()) && emulator != null) {
      return new File(emulator.getAltSoundFolder(), this.getRomAlias());
    }
    if (!StringUtils.isEmpty(this.getRom()) && emulator != null) {
      return new File(emulator.getAltSoundFolder(), this.getRom());
    }
    return null;
  }


  @Nullable
  @JsonIgnore
  public File getCfgFile() {
    if (!StringUtils.isEmpty(this.getRom())) {
      return new File(new File(emulator.getMameFolder(), "cfg"), this.getRom() + ".cfg");
    }
    return null;
  }

  @Nullable
  @JsonIgnore
  public File getAltColorFolder() {
    if (!StringUtils.isEmpty(this.getRomAlias()) && emulator != null) {
      return new File(new File(emulator.getMameFolder(), "altcolor"), this.getRomAlias());
    }
    if (!StringUtils.isEmpty(this.getRom()) && emulator != null) {
      return new File(new File(emulator.getMameFolder(), "altcolor"), this.getRom());
    }
    return null;
  }


  @Nullable
  @JsonIgnore
  public File getMusicFolder() {
    if (!StringUtils.isEmpty(this.getRom())) {
      return new File(emulator.getMusicFolder(), this.getRom());
    }
    return null;
  }

  public boolean isRomExists() {
    if (!StringUtils.isEmpty(this.getRom()) && emulator.getRomDirectory() != null) {
      File romFile = new File(emulator.getRomFolder(), this.getRom() + ".zip");
      if (romFile.exists()) {
        return true;
      }
    }

    return false;
  }

  @NonNull
  @JsonIgnore
  public File getBAMCfgFile() {
    String baseName = FilenameUtils.getBaseName(this.getGameFileName());
    return new File(emulator.getInstallationFolder(), "BAM/cfg/" + baseName + ".cfg");
  }

  @NonNull
  @JsonIgnore
  public File getDirectB2SFile() {
    String baseName = FilenameUtils.getBaseName(this.getGameFileName());
    return new File(getGameFile().getParentFile(), baseName + ".directb2s");
  }

  @NonNull
  @JsonIgnore
  public String getDirectB2SFilename() {
    String baseName = FilenameUtils.removeExtension(this.getGameFileName());
    return baseName + ".directb2s";
  }

  public int getNbDirectB2S() {
    return nbDirectB2S;
  }

  public void setNbDirectB2S(int nbDirectB2S) {
    this.nbDirectB2S = nbDirectB2S;
  }

  @NonNull
  @JsonIgnore
  public File getNvRamFile() {
    File nvRamFolder = new File(emulator.getMameFolder(), "nvram");

    String rom = getRom();
    File defaultNvRam = new File(nvRamFolder, rom + ".nv");
    if (defaultNvRam.exists() && getNvOffset() == 0) {
      return defaultNvRam;
    }

    //if the text file exists, the version matches with the current table, so this one was played last and the default nvram has the latest score
    File versionTextFile = new File(nvRamFolder, getRom() + " v" + getNvOffset() + ".txt");
    if (versionTextFile.exists()) {
      return defaultNvRam;
    }

    //else, we can check if a nv file with the alias and version exists which means the another table with the same rom has been played after this table
    File nvOffsettedNvRam = new File(nvRamFolder, rom + " v" + getNvOffset() + ".nv");
    if (nvOffsettedNvRam.exists()) {
      return nvOffsettedNvRam;
    }

    return defaultNvRam;
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
