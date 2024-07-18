package de.mephisto.vpin.server.games;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.connectors.vps.model.VPSChanges;
import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.frontend.FrontendMedia;
import de.mephisto.vpin.restclient.highscores.HighscoreType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.util.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

  private GameEmulator emulator;
  private int emulatorId;

  private File gameFile;

  private ValidationState validationState;
  private List<Integer> ignoredValidations = new ArrayList<>();
  private HighscoreType highscoreType;
  private boolean altSoundAvailable;
  private AltColorTypes altColorType;

  private String assets;
  private PupPack pupPack;
  private List<Integer> playlists = new ArrayList<>();

  private String pupPackName;
  private Long templateId;
  private String extTableId;
  private String extTableVersionId;
  private String extVersion;
  private String notes;
  private VPSChanges vpsChanges = new VPSChanges();

  public Game() {
  }

  public String getPupPackName() {
    if (this.pupPack != null) {
      return this.pupPack.getName();
    }
    return pupPackName;
  }

  public Date getDateUpdated() {
    return dateUpdated;
  }

  public void setDateUpdated(Date dateUpdated) {
    this.dateUpdated = dateUpdated;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public boolean isVpxGame() {
    return this.emulator.isVpxEmulator();
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

  @JsonIgnore
  public Image getWheelImage() {
    FrontendMediaItem frontendMediaItem = getGameMedia().getDefaultMediaItem(VPinScreen.Wheel);
    Image image = null;
    if (frontendMediaItem != null) {
      try {
        BufferedImage bufferedImage = ImageUtil.loadImage(frontendMediaItem.getFile());
        image = SwingFXUtils.toFXImage(bufferedImage, null);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return image;
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
  @NonNull
  public File getMediaFolder(@NonNull VPinScreen screen) {
    return this.emulator.getGameMediaFolder(FilenameUtils.getBaseName(gameFileName), screen);
  }

  @NonNull
  public List<File> getMediaFiles(@NonNull VPinScreen screen) {
    String baseFilename = getGameName();
    File mediaFolder = getMediaFolder(screen);
    //keep null check for serialization
    if (mediaFolder != null && mediaFolder.exists()) {
      File[] mediaFiles = mediaFolder.listFiles((dir, name) -> name.toLowerCase().startsWith(baseFilename.toLowerCase()));
      if (mediaFiles != null) {
        Pattern plainMatcher = Pattern.compile(Pattern.quote(baseFilename) + "\\d{0,2}\\.[a-zA-Z0-9]*");
        Pattern screenMatcher = Pattern.compile(Pattern.quote(baseFilename) + "\\d{0,2}\\(.*\\)\\.[a-zA-Z0-9]*");
        return Arrays.stream(mediaFiles).filter(f -> plainMatcher.matcher(f.getName()).matches() || screenMatcher.matcher(f.getName()).matches()).collect(Collectors.toList());
      }
    }
    return Collections.emptyList();
  }

  @NonNull
  public FrontendMedia getGameMedia() {
    FrontendMedia frontendMedia = new FrontendMedia();
    VPinScreen[] screens = VPinScreen.values();
    for (VPinScreen screen : screens) {
      List<FrontendMediaItem> itemList = new ArrayList<>();
      List<File> mediaFiles = getMediaFiles(screen);
      for (File file : mediaFiles) {
        FrontendMediaItem item = new FrontendMediaItem(this.getId(), screen, file);
        itemList.add(item);
      }
      frontendMedia.getMedia().put(screen.name(), itemList);
    }
    return frontendMedia;
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

  public boolean isPovAvailable() {
    return this.getPOVFile().exists();
  }

  public boolean isIniAvailable() {
    return this.getIniFile().exists();
  }

  public boolean isResAvailable() {
    return this.getResFile().exists();
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
    return getDirectB2SFile().exists();
  }

  public boolean isGameFileAvailable() {
    return this.getGameFile().exists();
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
    return new File(getGameFile().getParentFile(), baseName + ".directb2s");
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
