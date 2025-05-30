package de.mephisto.vpin.server.games;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.mephisto.vpin.connectors.vps.model.VpsFeatures;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.validation.ValidationState;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GameEmulator {
  private final static String VPREG_STG = "VPReg.stg";

  private EmulatorType type;
  /**
   * The internal folder name, not used for display
   */
  private String safeName;
  private String name;
  private String description;
  private String installationDirectory;
  private String gamesDirectory;
  private String mediaDirectory;

  private String mameDirectory;
  private String romDirectory;
  private String nvramDirectory;
  private String cfgDirectory;

  private int id;
  private boolean enabled;

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

  public void setSafeName(String safeName) {
    this.safeName = safeName;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setInstallationDirectory(String installationDirectory) {
    this.installationDirectory = installationDirectory;
  }

  public void setGamesDirectory(String gamesDirectory) {
    this.gamesDirectory = gamesDirectory;
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

  @JsonIgnore
  public boolean isOtherEmulator() {
    return type.isOther();
  }

  public String getGameFileName(@NonNull File file) {
    return file.getAbsolutePath().substring(getGamesFolder().getAbsolutePath().length() + 1);
  }

  public String getGameExt() {
    return gameExt;
  }

  public String getMediaDirectory() {
    return mediaDirectory;
  }

  public String getMameDirectory() {
    return mameDirectory;
  }

  public void setMameDirectory(String mameDirectory) {
    this.mameDirectory = mameDirectory;
  }

  public String getNvramDirectory() {
    return nvramDirectory;
  }

  public void setNvramDirectory(String nvramDirectory) {
    this.nvramDirectory = nvramDirectory;
  }

  public String getCfgDirectory() {
    return cfgDirectory;
  }

  public void setCfgDirectory(String cfgDirectory) {
    this.cfgDirectory = cfgDirectory;
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

  @Nullable
  public String getGamesDirectory() {
    return gamesDirectory;
  }

  public String getSafeName() {
    return safeName;
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
    return new File(getNvramDirectory());
  }

  @NonNull
  @JsonIgnore
  public File getCfgFolder() {
    return new File(getCfgDirectory());
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

  public String getAltSoundDirectory() {
    return getAltSoundFolder().getAbsolutePath();
  }

  @NonNull
  @JsonIgnore
  public File getAltColorFolder() {
    return new File(getMameFolder(), "altcolor");
  }

  public String getAltColorDirectory() {
    return getAltColorFolder().getAbsolutePath();
  }

  @NonNull
  @JsonIgnore
  public File getRomFolder() {
    return new File(romDirectory);
  }

  @NonNull
  @JsonIgnore
  public File getMameFolder() {
    String dir = getMameDirectory();
    if (!StringUtils.isEmpty(dir)) {
      return new File(dir);
    }
    return new File("./");
  }

  @NonNull
  @JsonIgnore
  public File getUserFolder() {
    return new File(getInstallationFolder(), "User");
  }

  @Nullable
  @JsonIgnore
  public File getInstallationFolder() {
    if (!StringUtils.isEmpty(installationDirectory)) {
      return new File(installationDirectory);
    }
    return new File("./");
  }

  @NonNull
  @JsonIgnore
  public File getGamesFolder() {
    if (getGamesDirectory() != null) {
      return new File(getGamesDirectory());
    }
    return new File("./");
  }

  @Nullable
  @JsonIgnore
  public File getTableBackupsFolder() {
    if (isVpxEmulator() && !StringUtils.isEmpty(getGamesDirectory())) {
      return new File(new File(getGamesDirectory()).getParentFile(), "Tables (Backups)/");
    }
    return null;
  }


  public String[] getVpsEmulatorFeatures() {
    if (this.type != null) {
      switch (this.type) {
        case VisualPinball: {
          return new String[]{VpsFeatures.VPX};
        }
        case FuturePinball: {
          return new String[]{VpsFeatures.FP};
        }
        case ZenFX: {
          return new String[]{VpsFeatures.FX, VpsFeatures.FX2, VpsFeatures.FX3};
        }
        case ZenFX2: {
          return new String[]{VpsFeatures.FX, VpsFeatures.FX2, VpsFeatures.FX3};
        }
        case ZenFX3: {
          return new String[]{VpsFeatures.FX, VpsFeatures.FX2, VpsFeatures.FX3};
        }
      }
    }
    return new String[]{VpsFeatures.VPX};
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
    return "\"" + this.name + "\" (ID: " + this.id + "/" + this.safeName + " [" + gamesDirectory + "])";
  }
}
