package de.mephisto.vpin.restclient;

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

  private String thumbnailBase64;

  public String getThumbnailBase64() {
    return thumbnailBase64;
  }

  public int getB2sElements() {
    return b2sElements;
  }

  public void setB2sElements(int b2sElements) {
    this.b2sElements = b2sElements;
  }

  public void setThumbnailBase64(String thumbnailBase64) {
    this.thumbnailBase64 = thumbnailBase64;
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
