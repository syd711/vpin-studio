package de.mephisto.vpin.restclient.emulators;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.validation.ValidationState;

import java.util.ArrayList;
import java.util.List;

public class GameEmulatorRepresentation {
  private EmulatorType type;
  private String safeName;
  private String name;
  private String description;
  private String installationDirectory;
  private String gamesDirectory;
  private String mediaDirectory;
  private String romDirectory;
  private int id;
  private boolean enabled;

  private String exeName;
  private String exeParameters;
  private String gameExt;

  private GameEmulatorScript launchScript;
  private GameEmulatorScript exitScript;

  public String[] vpsEmulatorFeatures;

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

  public String getSafeName() {
    return safeName;
  }

  public void setSafeName(String safeName) {
    this.safeName = safeName;
  }

  public EmulatorType getType() {
    return type;
  }

  public void setType(EmulatorType type) {
    this.type = type;
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

  public String[] getVpsEmulatorFeatures() {
    return vpsEmulatorFeatures;
  }

  public void setVpsEmulatorFeatures(String[] vpsEmulatorFeatures) {
    this.vpsEmulatorFeatures = vpsEmulatorFeatures;
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
