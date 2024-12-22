package de.mephisto.vpin.restclient.games;

import de.mephisto.vpin.connectors.vps.model.VpsFeatures;
import de.mephisto.vpin.restclient.frontend.EmulatorType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameEmulatorRepresentation {
  private int id;
  private String name;
  private String descriptions;

  private EmulatorType emulatorType;

  private String installationDirectory;
  private String tablesDirectory;
  private String userDirectory;

  private String altSoundDirectory;
  private String altColorDirectory;

  private String mameDirectory;
  private String nvramDirectory;
  private String romDirectory;

  private String launchScript;
  private String exitScript;

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

  private List<String> altExeNames = new ArrayList<>();

  public List<String> getAltExeNames() {
    return altExeNames;
  }

  public void setAltExeNames(List<String> altExeNames) {
    this.altExeNames = altExeNames;
  }

  public String getRomDirectory() {
    return romDirectory;
  }

  public void setRomDirectory(String romDirectory) {
    this.romDirectory = romDirectory;
  }

  public boolean isFpEmulator() {
    return emulatorType.isFpEmulator();
  }

  public boolean isVpxEmulator() {
    return emulatorType.isVpxEmulator();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EmulatorType getEmulatorType() {
    return emulatorType;
  }

  public void setEmulatorType(EmulatorType type) {
    this.emulatorType = type;
  }

  public String getMameDirectory() {
    return mameDirectory;
  }

  public void setMameDirectory(String mameDirectory) {
    this.mameDirectory = mameDirectory;
  }

  public String getUserDirectory() {
    return userDirectory;
  }

  public void setUserDirectory(String userDirectory) {
    this.userDirectory = userDirectory;
  }

  public String getNvramDirectory() {
    return nvramDirectory;
  }

  public void setNvramDirectory(String nvramDirectory) {
    this.nvramDirectory = nvramDirectory;
  }

  public String getAltSoundDirectory() {
    return altSoundDirectory;
  }

  public void setAltSoundDirectory(String altSoundDirectory) {
    this.altSoundDirectory = altSoundDirectory;
  }

  public String getAltColorDirectory() {
    return altColorDirectory;
  }

  public void setAltColorDirectory(String altColorDirectory) {
    this.altColorDirectory = altColorDirectory;
  }

  public String getInstallationDirectory() {
    return installationDirectory;
  }

  public void setInstallationDirectory(String installationDirectory) {
    this.installationDirectory = installationDirectory;
  }

  public String getTablesDirectory() {
    return tablesDirectory;
  }

  public void setTablesDirectory(String tablesDirectory) {
    this.tablesDirectory = tablesDirectory;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getDescriptions() {
    return descriptions;
  }

  public void setDescriptions(String descriptions) {
    this.descriptions = descriptions;
  }

  public List<String> getVpsEmulatorFeatures() {
    if (this.getEmulatorType() != null) {
      switch (this.emulatorType) {
        case VisualPinball: {
          return Arrays.asList(VpsFeatures.VPX);
        }
        case FuturePinball: {
          return Arrays.asList(VpsFeatures.FP);
        }
        case ZenFX:
        case ZenFX2:
        case ZenFX3: {
          return Arrays.asList(VpsFeatures.FX, VpsFeatures.FX3);
        }
      }
    }

    return Arrays.asList(VpsFeatures.VPX);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GameEmulatorRepresentation)) return false;

    GameEmulatorRepresentation that = (GameEmulatorRepresentation) o;

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
