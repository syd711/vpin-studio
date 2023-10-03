package de.mephisto.vpin.restclient.popper;

public class Emulator {

  private String name;
  private String description;
  private int id;
  private String dirMedia;
  private String dirGames;
  private String dirRoms;
  private String emuLaunchDir;
  private String gamesExt;
  private boolean visible;

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
    return isVisualPinball(this.name);
  }

  public static boolean isVisualPinball(String name) {
    return name.toLowerCase().startsWith(EmulatorType.VISUAL_PINBALL_X.toLowerCase())
        || name.toLowerCase().startsWith(EmulatorType.VISUAL_PINBALL.toLowerCase())
        || name.toLowerCase().startsWith(EmulatorType.VISUALPINBALL.toLowerCase())
        || name.toLowerCase().startsWith(EmulatorType.VISUALPINBALLX.toLowerCase());
  }
}
