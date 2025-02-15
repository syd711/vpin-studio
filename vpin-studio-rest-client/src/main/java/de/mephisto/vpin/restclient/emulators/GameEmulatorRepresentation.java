package de.mephisto.vpin.restclient.emulators;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.connectors.vps.model.VpsFeatures;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.validation.ValidationState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameEmulatorRepresentation {
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

  private String exeName;
  private String exeParameters;
  private String gameExt;

  private String userDirectory;
  private String altSoundDirectory;
  private String altColorDirectory;
  private String nvramDirectory;

  private String launchScript;
  private String exitScript;
  private List<ValidationState> validationStates = new ArrayList<>();

  public List<ValidationState> getValidationStates() {
    return validationStates;
  }

  public void setValidationStates(List<ValidationState> validationStates) {
    this.validationStates = validationStates;
  }

  public String getGameExt() {
    return gameExt;
  }

  public void setGameExt(String gameExt) {
    this.gameExt = gameExt;
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

  public String getRomDirectory() {
    return romDirectory;
  }

  public void setRomDirectory(String romDirectory) {
    this.romDirectory = romDirectory;
  }

  public boolean isFpEmulator() {
    return type.isFpEmulator();
  }

  public boolean isFxEmulator() {
    return type.isFxEmulator();
  }

  public boolean isVpxEmulator() {
    return type.isVpxEmulator();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EmulatorType getType() {
    return type;
  }

  public void setType(EmulatorType type) {
    this.type = type;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getMameDirectory() {
    return mameDirectory;
  }

  public void setMameDirectory(String mameDirectory) {
    this.mameDirectory = mameDirectory;
  }

  public String getMediaDirectory() {
    return mediaDirectory;
  }

  public void setMediaDirectory(String mediaDirectory) {
    this.mediaDirectory = mediaDirectory;
  }

  public String getExeName() {
    return exeName;
  }

  public void setExeName(String exeName) {
    this.exeName = exeName;
  }

  public String getExeParameters() {
    return exeParameters;
  }

  public void setExeParameters(String exeParameters) {
    this.exeParameters = exeParameters;
  }

  public String getInstallationDirectory() {
    return installationDirectory;
  }

  public void setInstallationDirectory(String installationDirectory) {
    this.installationDirectory = installationDirectory;
  }

  public String getGamesDirectory() {
    return gamesDirectory;
  }

  public void setGamesDirectory(String gamesDirectory) {
    this.gamesDirectory = gamesDirectory;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getNvramDirectory() {
    return nvramDirectory;
  }

  public void setNvramDirectory(String nvramDirectory) {
    this.nvramDirectory = nvramDirectory;
  }

  public String getUserDirectory() {
    return userDirectory;
  }

  public void setUserDirectory(String userDirectory) {
    this.userDirectory = userDirectory;
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

  public List<String> getVpsEmulatorFeatures() {
    if (this.getType() != null) {
      switch (this.type) {
        case VisualPinball: {
          return Arrays.asList(VpsFeatures.VPX);
        }
        case FuturePinball: {
          return Arrays.asList(VpsFeatures.FP);
        }
        case ZenFX: {
          return Arrays.asList(VpsFeatures.FX, VpsFeatures.FX2, VpsFeatures.FX3);
        }
        case ZenFX2: {
          return Arrays.asList(VpsFeatures.FX, VpsFeatures.FX2, VpsFeatures.FX3);
        }
        case ZenFX3: {
          return Arrays.asList(VpsFeatures.FX, VpsFeatures.FX2, VpsFeatures.FX3);
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
