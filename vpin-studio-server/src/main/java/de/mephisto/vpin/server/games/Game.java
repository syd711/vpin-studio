package de.mephisto.vpin.server.games;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.server.popper.Emulator;
import de.mephisto.vpin.server.popper.GameMedia;
import de.mephisto.vpin.server.popper.GameMediaItem;
import de.mephisto.vpin.server.system.SystemService;
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
  private String originalRom;
  private String gameDisplayName;
  private String gameFileName;
  private String tableName;
  private int id;
  private int nvOffset;
  private String hsFileName;
  private Emulator emulator;

  private File gameFile;
  private File povFile;

  private Date lastPlayed;
  private int numberPlays;
  private int validationState;
  private String ignoredValidations;
  private int volume;

  private List<GameAsset> assets = new ArrayList<>();

  private SystemService systemService;

  public Game() {

  }

  public Game(@NonNull SystemService systemService) {
    this.systemService = systemService;
  }


  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public List<GameAsset> getAssets() {
    return assets;
  }

  public void setAssets(List<GameAsset> assets) {
    this.assets = assets;
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

  @JsonIgnore
  public SystemService getSystemService() {
    return systemService;
  }

  public void setSystemService(SystemService systemService) {
    this.systemService = systemService;
  }

  @NonNull
  public File getPinUPMediaFolder(@NonNull PopperScreen screen) {
    File emulatorMediaFolder = new File(this.emulator.getMediaDir());
    return new File(emulatorMediaFolder, screen.name());
  }

  @Nullable
  public File getPinUPMedia(@NonNull PopperScreen screen) {
    String baseName = FilenameUtils.getBaseName(getGameFileName());
    File[] mediaFiles = getPinUPMediaFolder(screen).listFiles((dir, name) -> FilenameUtils.getBaseName(name).equals(baseName));
    if (mediaFiles != null && mediaFiles.length > 0) {
      return mediaFiles[0];
    }
    return null;
  }

  @NonNull
  public GameMedia getGameMedia() {
    GameMedia gameMedia = new GameMedia();
    PopperScreen[] screens = PopperScreen.values();
    for (PopperScreen screen : screens) {
      File mediaFile = getPinUPMedia(screen);
      if (mediaFile != null) {
        GameMediaItem item = new GameMediaItem(this, screen, mediaFile);
        gameMedia.getMedia().put(screen.name(), item);
      }
    }
    return gameMedia;
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

  @Nullable
  @JsonIgnore
  public File getPOVFile() {
    return povFile;
  }

  public boolean isPOV() {
    return this.povFile != null && this.povFile.exists();
  }

  public void setPOVFile(File povFile) {
    this.povFile = povFile;
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

  public boolean isDirectB2SAsMediaAvailable() {
    String name = FilenameUtils.getBaseName(this.getGameFileName());
    String directB2SName = name + ".directb2s";
    return new File(systemService.getDirectB2SMediaFolder(), directB2SName).exists();
  }

  public boolean isPupPackAvailable() {
    if (StringUtils.isEmpty(this.getRom())) {
      return false;
    }

    File pupVideos = new File(systemService.getPinUPSystemFolder(), "PUPVideos");
    File pupPackFolder = new File(pupVideos, getRom());
    return pupPackFolder.exists() && pupPackFolder.listFiles().length > 1;
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
    if (!StringUtils.isEmpty(this.getRom())) {
      return new File(systemService.getMameRomFolder(), this.getRom() + ".zip");
    }
    return null;
  }

  public boolean isRomExists() {
    return getRomFile() != null && getRomFile().exists();
  }

  @NonNull
  @JsonIgnore
  public File getDirectB2SFile() {
    String baseName = FilenameUtils.getBaseName(this.getGameFileName());
    return new File(systemService.getVPXTablesFolder(), baseName + ".directb2s");
  }

  @NonNull
  @JsonIgnore
  public File getDirectB2SMediaFile() {
    String baseName = FilenameUtils.getBaseName(this.getGameFileName());
    return new File(systemService.getDirectB2SMediaFolder(), baseName + ".directb2s");
  }

  @NonNull
  @JsonIgnore
  public File getCroppedDirectB2SBackgroundImage() {
    String targetName = FilenameUtils.getBaseName(getGameFileName()) + ".png";
    return new File(systemService.getB2SCroppedImageFolder(), targetName);
  }


  @NonNull
  @JsonIgnore
  public File getRawDirectB2SBackgroundImage() {
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
