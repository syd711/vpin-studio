package de.mephisto.vpin.restclient.games;

public class GameListItem {
  private String name;
  private String fileName;
  private long fileSize;
  private int emuId;

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

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
