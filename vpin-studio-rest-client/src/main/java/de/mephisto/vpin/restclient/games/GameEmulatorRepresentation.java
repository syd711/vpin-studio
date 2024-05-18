package de.mephisto.vpin.restclient.games;

public class GameEmulatorRepresentation {
  private int id;
  private String name;
  private String descriptions;

  private String installationDirectory;
  private String tablesDirectory;
  private String userDirectory;

  private String altSoundDirectory;
  private String altColorDirectory;

  private String mediaDirectory;

  private String mameDirectory;
  private String nvramDirectory;


  private String backglassServerFolder;
  public boolean vpxEmulator;

  public String getBackglassServerFolder() {
    return backglassServerFolder;
  }

  public void setBackglassServerFolder(String backglassServerFolder) {
    this.backglassServerFolder = backglassServerFolder;
  }

  public boolean isVpxEmulator() {
    return vpxEmulator;
  }

  public void setVpxEmulator(boolean vpxEmulator) {
    this.vpxEmulator = vpxEmulator;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setMediaDirectory(String mediaDirectory) {
    this.mediaDirectory = mediaDirectory;
  }

  public String getMediaDirectory() {
    return mediaDirectory;
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
