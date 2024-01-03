package de.mephisto.vpin.restclient.popper;

public class Emulator {
  private String name;
  private String description;
  private String displayName;
  private int id;
  private String dirMedia;
  private String dirGames;
  private String dirRoms;
  private String emuLaunchDir;
  private String gamesExt;
  private boolean visible;

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
    return isVisualPinball(this.name, this.displayName, this.description);
  }

  public static boolean isVisualPinball(String name, String displayName, String description) {
    if (matchesVPX(name)) {
      return true;
    }
    if (matchesVPX(displayName)) {
      return true;
    }
    if (matchesVPX(description)) {
      return true;
    }
    return false;
  }

  private static boolean matchesVPX(String name) {
    if (name == null) {
      return false;
    }

    return name.toLowerCase().startsWith(EmulatorType.VISUAL_PINBALL_X.toLowerCase())
      || name.toLowerCase().startsWith(EmulatorType.VISUAL_PINBALL.toLowerCase())
      || name.toLowerCase().startsWith(EmulatorType.VISUALPINBALL.toLowerCase())
      || name.toLowerCase().startsWith(EmulatorType.VISUALPINBALLX.toLowerCase());
  }
}
