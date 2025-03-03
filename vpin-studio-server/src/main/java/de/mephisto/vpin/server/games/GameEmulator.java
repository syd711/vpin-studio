package de.mephisto.vpin.server.games;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.mame.MameUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GameEmulator {
  private final static String VPREG_STG = "VPReg.stg";

  private EmulatorType type;
  private String name;
  private String description;
  private String displayName;
  private String installationDirectory;
  private String gamesDirectory;
  private String mameDirectory;
  private String mediaDirectory;
  private String romDirectory;
  private int id;
  private boolean enabled;

  /** The internal/technical name, not used for display */
  private String internalName;

  private String exeName;
  private String exeParameters;
  private String gameExt;

  private GameEmulatorScript launchScript = new GameEmulatorScript();
  private GameEmulatorScript exitScript = new GameEmulatorScript();

  private List<ValidationState> validationStates = new ArrayList<>();

  public List<ValidationState> getValidationStates() {
    return validationStates;
  }

  public void setValidationStates(List<ValidationState> validationStates) {
    this.validationStates = validationStates;
  }

  /**
   * optional database name, used by pinballY for instance
   */
  private String database;

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }

  public void setType(EmulatorType type) {
    this.type = type;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setInstallationDirectory(String installationDirectory) {
    this.installationDirectory = installationDirectory;
  }

  public void setGamesDirectory(String gamesDirectory) {
    this.gamesDirectory = gamesDirectory;
  }

  public void setMameDirectory(String mameDirectory) {
    this.mameDirectory = mameDirectory;
  }

  public void setMediaDirectory(String mediaDirectory) {
    this.mediaDirectory = mediaDirectory;
  }

  public void setId(int id) {
    this.id = id;
  }

  public void setExeName(String exeName) {
    this.exeName = exeName;
  }

  public void setExeParameters(String exeParameters) {
    this.exeParameters = exeParameters;
  }

  public void setGameExt(String gameExt) {
    this.gameExt = gameExt;
  }

  public EmulatorType getType() {
    return type;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public GameEmulatorScript getLaunchScript() {
    return launchScript;
  }

  public void setLaunchScript(GameEmulatorScript launchScript) {
    this.launchScript = launchScript;
  }

  public GameEmulatorScript getExitScript() {
    return exitScript;
  }

  public void setExitScript(GameEmulatorScript exitScript) {
    this.exitScript = exitScript;
  }

  @JsonIgnore
  public boolean isVpxEmulator() {
    return type.isVpxEmulator();
  }

  @JsonIgnore
  public boolean isFpEmulator() {
    return type.isFpEmulator();
  }

  @JsonIgnore
  public boolean isFxEmulator() {
    return type.isFxEmulator();
  }

  @JsonIgnore
  public boolean isMameEmulator() {
    return type.isMameEmulator();
  }

  public String getGameFileName(@NonNull File file) {
    return file.getAbsolutePath().substring(getGamesFolder().getAbsolutePath().length() + 1);
  }

  public String getGameExt() {
    return gameExt;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getMediaDirectory() {
    return mediaDirectory;
  }

  public String getMameDirectory() {
    return mameDirectory;
  }

  public String getRomDirectory() {
    return romDirectory;
  }

  public void setRomDirectory(String romDirectory) {
    this.romDirectory = romDirectory;
  }

  public String getInstallationDirectory() {
    return installationDirectory;
  }

  public String getGamesDirectory() {
    return gamesDirectory;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public String getExeName() {
    return exeName;
  }

  public int getId() {
    return id;
  }
  
  public String getInternalName() {
    return internalName;
  }

  public void setInternalName(String internalName) {
    this.internalName = internalName;
  }

  public boolean isValid() {
    return !StringUtils.isEmpty(installationDirectory) && getInstallationFolder().exists();
  }

  @Nullable
  @JsonIgnore
  public File getExe() {
    if (exeName != null) {
      return new File(getInstallationFolder(), exeName);
    }
    return null;
  }

  public String getExeParameters() {
    return exeParameters;
  }

  @NonNull
  @JsonIgnore
  public File getNvramFolder() {
    if (isVpxEmulator()) {
      File registryFolder = new File(MameUtil.getNvRamFolder());
      if (registryFolder.exists()) {
        return registryFolder;
      }
    }
    return new File(getMameFolder(), "nvram");
  }

  @NonNull
  @JsonIgnore
  public File getCfgFolder() {
    return new File(getMameFolder(), "cfg");
  }

  @NonNull
  @JsonIgnore
  public File getVPRegFile() {
    return new File(getUserFolder(), VPREG_STG);
  }

  @NonNull
  @JsonIgnore
  public File getMusicFolder() {
    return new File(getInstallationFolder(), "Music");
  }

  @NonNull
  @JsonIgnore
  public File getAltSoundFolder() {
    return new File(getMameFolder(), "altsound");
  }

  @NonNull
  @JsonIgnore
  public File getAltColorFolder() {
    return new File(getMameFolder(), "altcolor");
  }

  @NonNull
  @JsonIgnore
  public File getRomFolder() {
    if (isVpxEmulator() && StringUtils.isEmpty(romDirectory)) {
      File romDir = new File(MameUtil.getRomsFolder());
      if (romDir.exists()) {
        return romDir;
      }
    }

    if (!StringUtils.isEmpty(romDirectory)) {
      return new File(romDirectory);
    }

    return new File(getMameFolder(), "roms");
  }

  @NonNull
  @JsonIgnore
  public File getMameFolder() {
    return new File(getInstallationFolder(), "VPinMAME");
  }

  @NonNull
  @JsonIgnore
  public File getUserFolder() {
    return new File(getInstallationFolder(), "User");
  }

  @NonNull
  @JsonIgnore
  public File getInstallationFolder() {
    return new File(installationDirectory);
  }

  @NonNull
  @JsonIgnore
  public File getGamesFolder() {
    return new File(getGamesDirectory());
  }

  @Nullable
  @JsonIgnore
  public File getTableBackupsFolder() {
    if (isVpxEmulator() && !StringUtils.isEmpty(getGamesDirectory())) {
      return new File(new File(getGamesDirectory()).getParentFile(), "Tables (Backups)/");
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GameEmulator)) return false;

    GameEmulator that = (GameEmulator) o;

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return "\"" + this.displayName + "\" (ID: " + this.id + "/" + this.name + " [" + gamesDirectory + "])";
  }
}
