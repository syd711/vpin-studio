package de.mephisto.vpin.server.games;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.mame.MameUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
  private String romsDirectory;
  private int id;
  private boolean enabled;

  private String exeName;
  private String exeParameters;
  private String gameExt;

  private String launchScript;
  private String exitScript;
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

  public String getRomsDirectory() {
    return romsDirectory;
  }

  public void setRomsDirectory(String romsDirectory) {
    this.romsDirectory = romsDirectory;
  }

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

  public String getLaunchScript() {
    return launchScript;
  }

  public void setLaunchScript(String launchScript) {
    this.launchScript = launchScript;
  }

  public String getExitScript() {
    return exitScript;
  }

  public void setExitScript(String exitScript) {
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

  public EmulatorType getEmulatorType() {
    return type;
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
    return romsDirectory;
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

  public List<String> getAltExeNames() {
    if (isVpxEmulator() && getInstallationFolder().exists()) {
      String[] exeFiles = getInstallationFolder().list((dir, name) -> name.endsWith(".exe") && name.toLowerCase().contains("vpin"));
      if (exeFiles == null) {
        exeFiles = new String[]{};
      }
      return Arrays.asList(exeFiles);
    }

    if (getExe() != null) {
      return Arrays.asList(getExe().getName());
    }

    return Collections.emptyList();
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
    if (isVpxEmulator() && StringUtils.isEmpty(romsDirectory)) {
      File romDir = new File(MameUtil.getRomsFolder());
      if (romDir.exists()) {
        return romDir;
      }
    }

    if (!StringUtils.isEmpty(romsDirectory)) {
      return new File(romsDirectory);
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
    return "\"" + this.name + "\" (ID: " + this.id + "/" + this.getName() + " [" + gamesDirectory + "])";
  }
}
