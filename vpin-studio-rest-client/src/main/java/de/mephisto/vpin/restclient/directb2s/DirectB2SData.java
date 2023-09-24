package de.mephisto.vpin.restclient.directb2s;

import java.util.Date;

public class DirectB2SData {
  private String name;
  private int tableType;
  private int numberOfPlayers;

  private String artwork;
  private String author;

  private int grillHeight;
  private int b2sElements;

  private long filesize;
  private Date modificationDate;

  private String backgroundBase64;
  private String dmdBase64;
  private int illuminations;

  public int getIlluminations() {
    return illuminations;
  }

  public void setIlluminations(int illuminations) {
    this.illuminations = illuminations;
  }

  public String getDmdBase64() {
    return dmdBase64;
  }

  public void setDmdBase64(String dmdBase64) {
    this.dmdBase64 = dmdBase64;
  }

  public String getBackgroundBase64() {
    return backgroundBase64;
  }

  public int getB2sElements() {
    return b2sElements;
  }

  public void setB2sElements(int b2sElements) {
    this.b2sElements = b2sElements;
  }

  public void setBackgroundBase64(String backgroundBase64) {
    this.backgroundBase64 = backgroundBase64;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getTableType() {
    return tableType;
  }

  public void setTableType(int tableType) {
    this.tableType = tableType;
  }

  public int getNumberOfPlayers() {
    return numberOfPlayers;
  }

  public void setNumberOfPlayers(int numberOfPlayers) {
    this.numberOfPlayers = numberOfPlayers;
  }

  public String getArtwork() {
    return artwork;
  }

  public void setArtwork(String artwork) {
    this.artwork = artwork;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public int getGrillHeight() {
    return grillHeight;
  }

  public void setGrillHeight(int grillHeight) {
    this.grillHeight = grillHeight;
  }

  public long getFilesize() {
    return filesize;
  }

  public void setFilesize(long filesize) {
    this.filesize = filesize;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public void setModificationDate(Date modificationDate) {
    this.modificationDate = modificationDate;
  }
}
