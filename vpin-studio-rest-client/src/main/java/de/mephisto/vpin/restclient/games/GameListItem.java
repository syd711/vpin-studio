package de.mephisto.vpin.restclient.games;

public class GameListItem {
  private String name;
  private String fileName;
  private int emuId;

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getEmuId() {
    return emuId;
  }

  public void setEmuId(int emuId) {
    this.emuId = emuId;
  }
}
