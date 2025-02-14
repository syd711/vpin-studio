package de.mephisto.vpin.server.games;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.frontend.Emulator;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.server.mame.MameUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameEmulator {
  private final static String VPREG_STG = "VPReg.stg";

  private File installationFolder;
  private File tablesFolder;
  private final File mameFolder;
  private final File userFolder;
  private final File altSoundFolder;
  private final File altColorFolder;
  private final File musicFolder;
  private final File nvramFolder;
  private final File cfgFolder;

  private File romFolder;

  private final EmulatorType type;
  private final String name;
  private final String description;
  private final String displayName;
  private final String installationDirectory;
  private final String tablesDirectory;
  private final String altSoundDirectory;
  private final String altColorDirectory;
  private final String romDirectory;
  private final String nvramDirectory;
  private final String mameDirectory;
  private final String userDirectory;
  private final String mediaDirectory;
  private final int id;
  private final boolean visible;
  private String exeName;
  private String exeParameters;
  private final String gameExt;

  private String launchScript;
  private String exitScript;

  private boolean enabled;

  public GameEmulator(@NonNull Emulator emulator) {
    this.id = emulator.getId();
    this.type = emulator.getType();
    this.name = emulator.getName();
    this.enabled = emulator.isEnabled();
    this.description = emulator.getDescription();
    this.displayName = emulator.getDisplayName();
    this.exeName = emulator.getExeName();
    this.exeParameters = emulator.getExeParameters();
    this.visible = emulator.isVisible();
    this.gameExt = emulator.getGamesExt();

    this.launchScript = emulator.getLaunchScript();
    this.exitScript = emulator.getExitScript();

    this.installationDirectory = emulator.getEmuLaunchDir();
    this.tablesDirectory = emulator.getDirGames();
    this.mediaDirectory = emulator.getDirMedia();

    if (emulator.getEmuLaunchDir() != null) {
      this.installationFolder = new File(emulator.getEmuLaunchDir());
    }

    if (emulator.getDirGames() != null) {
      this.tablesFolder = new File(emulator.getDirGames().trim());
    }

    this.musicFolder = new File(installationFolder, "Music");

    this.mameFolder = new File(installationFolder, "VPinMAME");
    this.mameDirectory = this.mameFolder.getAbsolutePath();

    this.userFolder = new File(installationFolder, "User");
    this.userDirectory = userFolder.getAbsolutePath();

    this.nvramFolder = new File(mameFolder, "nvram");
    this.nvramDirectory = this.nvramFolder.getAbsolutePath();

    this.cfgFolder = new File(mameFolder, "cfg");

    this.altSoundFolder = new File(mameFolder, "altsound");
    this.altSoundDirectory = this.altSoundFolder.getAbsolutePath();

    this.altColorFolder = new File(mameFolder, "altcolor");
    this.altColorDirectory = this.altColorFolder.getAbsolutePath();

    this.romFolder = new File(mameFolder, "roms");
    if (!StringUtils.isEmpty(emulator.getDirRoms())) {
      this.romFolder = new File(emulator.getDirRoms());
    }

    if (isVpxEmulator() && StringUtils.isEmpty(emulator.getDirRoms())) {
      File romDir = new File(MameUtil.getRomsFolder());
      if (romDir.exists()) {
        this.romFolder = romDir;
      }
    }

    this.romDirectory = this.romFolder.getAbsolutePath();
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
    return file.getAbsolutePath().substring(getTablesFolder().getAbsolutePath().length() + 1);
  }

  public String getGameExt() {
    return gameExt;
  }

  public String getDisplayName() {
    return displayName;
  }

  public boolean isVisible() {
    return visible;
  }

  public String getMediaDirectory() {
    return mediaDirectory;
  }

  public String getMameDirectory() {
    return mameDirectory;
  }

  public String getUserDirectory() {
    return userDirectory;
  }

  public String getNvramDirectory() {
    return nvramDirectory;
  }

  public String getAltSoundDirectory() {
    return altSoundDirectory;
  }

  public String getAltColorDirectory() {
    return altColorDirectory;
  }

  public String getRomDirectory() {
    return romDirectory;
  }

  public String getInstallationDirectory() {
    return installationDirectory;
  }

  public String getTablesDirectory() {
    return tablesDirectory;
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

  @Nullable
  @JsonIgnore
  public File getExe() {
    if (exeName != null) {
      return new File(installationFolder, exeName);
    }
    return null;
  }

  public String getExeParameters() {
    return exeParameters;
  }

  public List<String> getAltExeNames() {
    if (isVpxEmulator() && installationFolder != null && installationFolder.exists()) {
      String[] exeFiles = installationFolder.list((dir, name) -> name.endsWith(".exe") && name.toLowerCase().contains("vpin"));
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
    return nvramFolder;
  }

  @NonNull
  @JsonIgnore
  public File getCfgFolder() {
    return cfgFolder;
  }

  @NonNull
  @JsonIgnore
  public File getVPRegFile() {
    return new File(userFolder, VPREG_STG);
  }

  @NonNull
  @JsonIgnore
  public File getMusicFolder() {
    return musicFolder;
  }

  @NonNull
  @JsonIgnore
  public File getAltSoundFolder() {
    return altSoundFolder;
  }

  @NonNull
  @JsonIgnore
  public File getAltColorFolder() {
    return altColorFolder;
  }

  @NonNull
  @JsonIgnore
  public File getRomFolder() {
    return romFolder;
  }

  @NonNull
  @JsonIgnore
  public File getMameFolder() {
    return mameFolder;
  }

  @NonNull
  @JsonIgnore
  public File getUserFolder() {
    return userFolder;
  }

  @NonNull
  @JsonIgnore
  public File getInstallationFolder() {
    return installationFolder;
  }

  @NonNull
  @JsonIgnore
  public File getTablesFolder() {
    return tablesFolder;
  }

  @Nullable
  @JsonIgnore
  public File getTableBackupsFolder() {
    if (isVpxEmulator()) {
      return new File(tablesFolder.getParentFile(), "Tables (Backups)/");
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
    return "\"" + this.name + "\" (ID: " + this.id + "/" + this.getName() + " [" + tablesDirectory + "])";
  }
}
