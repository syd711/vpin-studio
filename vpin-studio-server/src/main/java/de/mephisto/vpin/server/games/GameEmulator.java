package de.mephisto.vpin.server.games;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.popper.Emulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameEmulator {
  private final static String VPREG_STG = "VPReg.stg";

  private final File installationFolder;
  private final File tablesFolder;
  private File backglassServerDirectory;
  private final File gameMediaFolder;
  private final File mameFolder;
  private final File userFolder;
  private final File altSoundFolder;
  private final File altColorFolder;
  private final File musicFolder;
  private final File nvramFolder;

  private File romFolder;

  private final String name;
  private final String description;
  private final String displayName;
  private final String installationDirectory;
  private final String tablesDirectory;
  private final String altSoundDirectory;
  private final String altColorDirectory;
  private final String nvramDirectory;
  private final String mameDirectory;
  private final String userDirectory;
  private final String mediaDirectory;
  private final int id;
  private final boolean visible;
  private final String vpxExeName;

  public GameEmulator(@NonNull Emulator emulator) {
    this.id = emulator.getId();
    this.name = emulator.getName();
    this.description = emulator.getDescription();
    this.displayName = emulator.getDisplayName();
    this.vpxExeName = emulator.getVpxExeName();
    this.visible = emulator.isVisible();

    this.installationDirectory = emulator.getEmuLaunchDir();
    this.tablesDirectory = emulator.getDirGames();
    this.mediaDirectory = emulator.getDirMedia();

    this.installationFolder = new File(emulator.getEmuLaunchDir());
    this.tablesFolder = new File(emulator.getDirGames());
    this.backglassServerDirectory = new File(emulator.getDirGames());

    this.gameMediaFolder = new File(emulator.getDirMedia());
    this.musicFolder = new File(installationFolder, "Music");

    this.mameFolder = new File(installationFolder, "VPinMAME");
    this.mameDirectory = this.mameFolder.getAbsolutePath();

    this.userFolder = new File(installationFolder, "User");
    this.userDirectory = userFolder.getAbsolutePath();

    this.nvramFolder = new File(mameFolder, "nvram");
    this.nvramDirectory = this.nvramFolder.getAbsolutePath();

    this.altSoundFolder = new File(mameFolder, "altsound");
    this.altSoundDirectory = this.altSoundFolder.getAbsolutePath();

    this.altColorFolder = new File(mameFolder, "altcolor");
    this.altColorDirectory = this.altColorFolder.getAbsolutePath();

    this.romFolder = new File(mameFolder, "roms");
    if (!StringUtils.isEmpty(emulator.getDirRoms())) {
      this.romFolder = new File(emulator.getDirRoms());
    }
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

  public File getVPXExe() {
    return new File(installationFolder, vpxExeName);
  }

  public boolean isVpx() {
    return true;
  }

  public List<String> getAltExeNames() {
    String[] exeFiles = installationFolder.list((dir, name) -> name.endsWith(".exe") && name.toLowerCase().contains("vpin"));
    if (exeFiles == null) {
      exeFiles = new String[]{};
    }
    return Arrays.asList(exeFiles);
  }

  @NonNull
  @JsonIgnore
  public File getBackglassServerDirectory() {
    return backglassServerDirectory;
  }

  public void setBackglassServerDirectory(@NonNull File backglassServerDirectory) {
    this.backglassServerDirectory = backglassServerDirectory;
  }

  @NonNull
  @JsonIgnore
  public File getB2STableSettingsXml() {
    return new File(this.tablesFolder, "B2STableSettings.xml");
  }

  @NonNull
  @JsonIgnore
  public File getNvramFolder() {
    return nvramFolder;
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
  public File getGameMediaFolder() {
    return gameMediaFolder;
  }

  @NonNull
  @JsonIgnore
  public File getTablesFolder() {
    return tablesFolder;
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
    return "\"" + this.name + "\" (ID: " + this.id + "/" + this.getVPXExe().getName() + ")";
  }
}
