package de.mephisto.vpin.server.games;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.popper.GameMedia;
import de.mephisto.vpin.server.popper.GameMediaItem;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Game {

  private String rom;
  private String romAlias;
  private String gameDisplayName;
  private String gameFileName;
  private String gameName;
  private String tableName;
  private String version;
  private boolean disabled;
  private boolean updateAvailable;
  private Date dateAdded;
  private int id;
  private int nvOffset;
  private String hsFileName;
  private GameEmulator emulator;

  private File gameFile;

  private ValidationState validationState;
  private List<Integer> ignoredValidations;
  private HighscoreType highscoreType;
  private boolean altSoundAvailable;
  private AltColorTypes altColorType;

  private String assets;
  private PupPack pupPack;
  private List<Integer> playlists;

  private SystemService systemService;
  private String extTableId;
  private String extTableVersionId;
  private String extVersion;
  private List<String> updates;

  public Game() {

  }

  public Game(@NonNull SystemService systemService) {
    this.systemService = systemService;
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

  public List<String> getUpdates() {
    return updates;
  }

  public void setUpdates(List<String> updates) {
    this.updates = updates;
  }

  @JsonIgnore
  @Nullable
  public PupPack getPupPack() {
    return pupPack;
  }

  public void setPupPack(PupPack pupPack) {
    this.pupPack = pupPack;
  }

  public boolean isPupPackAvailable() {
    return pupPack != null;
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

  public String getAssets() {
    return assets;
  }

  public void setAssets(String assets) {
    this.assets = assets;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  @NonNull
  @JsonIgnore
  public GameEmulator getEmulator() {
    return emulator;
  }

  public void setEmulator(@NonNull GameEmulator emulator) {
    this.emulator = emulator;
  }

  public List<Integer> getIgnoredValidations() {
    return ignoredValidations;
  }

  public void setIgnoredValidations(List<Integer> ignoredValidations) {
    this.ignoredValidations = ignoredValidations;
  }

  @JsonIgnore
  public SystemService getSystemService() {
    return systemService;
  }

  public void setSystemService(SystemService systemService) {
    this.systemService = systemService;
  }

  @JsonIgnore
  @NonNull
  public File getPinUPMediaFolder(@NonNull PopperScreen screen) {
    File emulatorMediaFolder = this.emulator.getGameMediaFolder();
    return new File(emulatorMediaFolder, screen.name());
  }

  @JsonIgnore
  @NonNull
  public File getUltraDMDFolder() {
    String folderName = this.getRom() + ".UltraDMD";
    return new File(this.getGameFile().getParentFile(), folderName);
  }

  @JsonIgnore
  @NonNull
  public File getFlexDMDFolder() {
    String folderName = getRom() + ".FlexDMD";
    return new File(this.getGameFile().getParentFile(), folderName);
  }

  @NonNull
  public List<File> getPinUPMedia(@NonNull PopperScreen screen) {
    String baseFilename = getGameName();
    File[] mediaFiles = getPinUPMediaFolder(screen).listFiles((dir, name) -> name.startsWith(baseFilename));
    if (mediaFiles != null) {
      Pattern plainMatcher  = Pattern.compile(Pattern.quote(baseFilename) + "\\d{0,2}\\.[a-zA-Z]*");
      Pattern screenMatcher  = Pattern.compile(Pattern.quote(baseFilename) + "\\d{0,2}\\(.*\\)\\.[a-zA-Z]*");
      return Arrays.stream(mediaFiles).filter(f -> plainMatcher.matcher(f.getName()).matches() || screenMatcher.matcher(f.getAbsolutePath()).matches()).collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

  @NonNull
  public GameMedia getGameMedia() {
    GameMedia gameMedia = new GameMedia();
    PopperScreen[] screens = PopperScreen.values();
    for (PopperScreen screen : screens) {
      List<GameMediaItem> itemList = new ArrayList<>();
      List<File> pinUPMedia = getPinUPMedia(screen);
      for (File file : pinUPMedia) {
        GameMediaItem item = new GameMediaItem(this, screen, file);
        itemList.add(item);
      }
      gameMedia.getMedia().put(screen.name(), itemList);
    }
    return gameMedia;
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
  public File getAlternateHighscoreTextFile() {
    if (!StringUtils.isEmpty(this.getTableName())) {
      String name = this.getTableName();
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
    return new File(emulator.getTablesFolder(), FilenameUtils.getBaseName(gameFileName) + ".pov");
  }


  @NonNull
  @JsonIgnore
  public File getIniFile() {
    return new File(emulator.getTablesFolder(), FilenameUtils.getBaseName(gameFileName) + ".ini");
  }

  @NonNull
  @JsonIgnore
  public File getResFile() {
    return new File(emulator.getTablesFolder(), FilenameUtils.getBaseName(gameFileName) + ".res");
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

  public int getEmulatorId() {
    return this.emulator.getId();
  }

  public boolean isPovAvailable() {
    return this.getPOVFile().exists();
  }

  public boolean isIniAvailable() {
    return this.getIniFile().exists();
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

  public boolean isDirectB2SAvailable() {
    String name = FilenameUtils.getBaseName(this.getGameFileName());
    String directB2SName = name + ".directb2s";
    return new File(emulator.getTablesFolder(), directB2SName).exists();
  }

  public boolean isGameFileAvailable() {
    return this.getGameFile().exists();
  }

  public boolean isDefaultBackgroundAvailable() {
    return this.getRawDefaultPicture() != null && this.getRawDefaultPicture().exists();
  }

  public String getHsFileName() {
    return hsFileName;
  }

  public void setHsFileName(String hsFileName) {
    this.hsFileName = hsFileName;
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

  public int getNvOffset() {
    return nvOffset;
  }

  public void setNvOffset(int nvOffset) {
    this.nvOffset = nvOffset;
  }

  public ValidationState getValidationState() {
    return validationState;
  }

  public void setValidationState(ValidationState validationState) {
    this.validationState = validationState;
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
    if (!StringUtils.isEmpty(this.getRom())) {
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
    if (!StringUtils.isEmpty(this.getRom())) {
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
    if (!StringUtils.isEmpty(this.getRom())) {
      File romFile = new File(emulator.getRomFolder(), this.getRom() + ".zip");
      if (romFile.exists()) {
        return true;
      }
    }
    return false;
  }

  @NonNull
  @JsonIgnore
  public File getDirectB2SFile() {
    String baseName = FilenameUtils.getBaseName(this.getGameFileName());
    return new File(emulator.getTablesFolder(), baseName + ".directb2s");
  }

  @Nullable
  @JsonIgnore
  public File getCroppedDefaultPicture() {
    if (this.getRom() != null) {
      File subFolder = new File(systemService.getB2SCroppedImageFolder(), this.getRom());
      return new File(subFolder, SystemService.DEFAULT_BACKGROUND);
    }
    return null;
  }

  @Nullable
  @JsonIgnore
  public File getDMDPicture() {
    if (this.getRom() != null) {
      File subFolder = new File(systemService.getB2SCroppedImageFolder(), this.getRom());
      return new File(subFolder, SystemService.DMD);
    }
    return null;
  }

  @NonNull
  @JsonIgnore
  public File getNvRamFile() {
    File nvRamFolder = new File(emulator.getMameFolder(), "nvram");

    String rom = getRom();
    File aliasedNvFile = new File(nvRamFolder, rom + ".nv");
    if (aliasedNvFile.exists() && getNvOffset() == 0) {
      return aliasedNvFile;
    }

    //else, we can check if a nv file with the alias and version exists
    File versionNVAliasedFile = new File(emulator.getMameFolder(), rom + " v" + getNvOffset() + ".nv");
    if (versionNVAliasedFile.exists()) {
      return versionNVAliasedFile;
    }

    //if the text file exists, the current nv file contains the highscore of this table
    File versionTextFile = new File(emulator.getMameFolder(), getRom() + " v" + getNvOffset() + ".txt");
    if (versionTextFile.exists()) {
      return versionTextFile;
    }

    return aliasedNvFile;
  }


  @Nullable
  @JsonIgnore
  public File getRawDefaultPicture() {
    if (this.getRom() != null) {
      File subFolder = new File(systemService.getB2SImageExtractionFolder(), this.getRom());
      return new File(subFolder, SystemService.DEFAULT_BACKGROUND);
    }
    return null;
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
