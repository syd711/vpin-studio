package de.mephisto.vpin.restclient.frontend;

public class Emulator {
  private String name;
  private String description;
  private String displayName;
  private int id;
  private String dirMedia;
  private String dirGames;
  private String dirRoms;
  /** opportunity for emulator to set a specific b2s folder, if null use dirGames */
  private String dirB2S;
  private String emuLaunchDir;
  /** the executable to run the table */
  private String exeName;
  private String gamesExt;
  private boolean visible;
  private boolean enabled = true;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getGamesExt() {
    return gamesExt;
  }

  public void setGamesExt(String gamesExt) {
    this.gamesExt = gamesExt;
  }

  public String getEmuLaunchDir() {
    return emuLaunchDir;
  }

  public void setEmuLaunchDir(String emuLaunchDir) {
    this.emuLaunchDir = emuLaunchDir;
  }
  
  public String getExeName() {
    return exeName;
  }

  public void setExeName(String exeName) {
    this.exeName = exeName;
  }


  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public String getDirGames() {
    return dirGames;
  }

  public void setDirGames(String dirGames) {
    this.dirGames = dirGames;
  }

  public String getDirB2S() {
    return dirB2S;
  }

  public void setDirB2S(String dirB2S) {
    this.dirB2S = dirB2S;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getDirMedia() {
    return dirMedia;
  }

  public void setDirMedia(String dirMedia) {
    this.dirMedia = dirMedia;
  }

  public String getDirRoms() {
    return dirRoms;
  }

  public void setDirRoms(String dirRoms) {
    this.dirRoms = dirRoms;
  }

  public boolean isVisualPinball() {
    return isVisualPinball(this.name, this.displayName, this.description, this.gamesExt);
  }

  public static boolean isVisualPinball(String name, String displayName, String description, String gamesExt) {
    if (matchesVPX(name)) {
      return true;
    }
    if (matchesVPX(displayName)) {
      return true;
    }
    if (matchesVPX(description)) {
      return true;
    }
    return String.valueOf(gamesExt).toLowerCase().contains("vpx");
  }

  private static boolean matchesVPX(String name) {
    if (name == null) {
      return false;
    }

    return name.toLowerCase().startsWith(EmulatorNames.VISUAL_PINBALL_X.toLowerCase())
      || name.toLowerCase().startsWith(EmulatorNames.VISUAL_PINBALL.toLowerCase())
      || name.toLowerCase().startsWith(EmulatorNames.VISUALPINBALL.toLowerCase())
      || name.toLowerCase().startsWith(EmulatorNames.VISUALPINBALLX.toLowerCase());
  }

  @Override
  public String toString() {
    return "Emulator \"" + this.name + "\" (EMUID #" + this.id + ")";
  }

}
