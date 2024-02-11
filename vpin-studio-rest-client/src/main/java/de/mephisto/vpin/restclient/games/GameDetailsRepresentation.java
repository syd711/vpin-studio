package de.mephisto.vpin.restclient.games;

public class GameDetailsRepresentation {
  private String romName;

  private String tableName;

  private String hsFileName;

  public String getRomName() {
    return romName;
  }

  public void setRomName(String romName) {
    this.romName = romName;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getHsFileName() {
    return hsFileName;
  }

  public void setHsFileName(String hsFileName) {
    this.hsFileName = hsFileName;
  }
}
