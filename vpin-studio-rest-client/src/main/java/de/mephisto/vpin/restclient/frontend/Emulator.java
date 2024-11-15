package de.mephisto.vpin.restclient.frontend;

public class Emulator {
  private String name;
  private String description;
  private String displayName;
  private int id;
  private String dirMedia;
  private String dirGames;
  private String dirRoms;

  /** optionnal database name, used by pinballY for instance */
  private String database;

  private EmulatorType type;

  private String emuLaunchDir;
  /**
   * the executable to run the table
   */
  private String exeName;
  private String exeParameters;

  private String gamesExt;
  private boolean visible;
  private boolean enabled = true;


  public Emulator(EmulatorType type) {
    this.type = type;
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

  public String getExeParameters() {
    return exeParameters;
  }

  public void setExeParameters(String exeParameters) {
    this.exeParameters = exeParameters;
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

  public String getDatabase() {
    return database;
  }

  public void setDatabase(String database) {
    this.database = database;
  }


  @Override
  public String toString() {
    return "Emulator \"" + this.name + "\" (EMUID #" + this.id + ")";
  }
}
