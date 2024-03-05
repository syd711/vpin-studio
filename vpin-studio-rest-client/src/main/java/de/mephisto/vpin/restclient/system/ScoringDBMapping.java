package de.mephisto.vpin.restclient.system;

public class ScoringDBMapping {
  private String rom;
  private String tableName;
  private String textFile;

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public String getTextFile() {
    return textFile;
  }

  public void setTextFile(String textFile) {
    this.textFile = textFile;
  }
}
