package de.mephisto.vpin.restclient.directb2s;

import java.util.Date;

public class DirectB2SData {
  private String filename;
  private int emulatorId;
  private int gameId;

  private String name;
  private int tableType;
  private int numberOfPlayers;

  private String artwork;
  private String author;

  private int grillHeight;
  private int b2sElements;

  private long filesize;
  private Date modificationDate;

  private boolean backgroundAvailable;
  private boolean dmdImageAvailable;

  private int illuminations;

  private int scores;

  public static String getTableType(int type) {
    switch (type) {
      case 1: {
        return "Electro Mechanical";
      }
      case 2: {
        return "Solid State Electronic";
      }
      case 3: {
        return "Solid State Electronic with DMD";
      }
      case 4: {
        return "Original";
      }
      default: {
        return "-";
      }
    }
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public int getIlluminations() {
    return illuminations;
  }

  public void setIlluminations(int illuminations) {
    this.illuminations = illuminations;
  }

  public boolean isBackgroundAvailable() {
    return backgroundAvailable;
  }
  public void setBackgroundAvailable(boolean hasBackgroundImage) {
    this.backgroundAvailable = hasBackgroundImage;
  }

  public boolean isDmdImageAvailable() {
    return dmdImageAvailable;
  }
  public void setDmdImageAvailable(boolean hasDmdImage) {
    this.dmdImageAvailable = hasDmdImage;
  }

  public int getB2sElements() {
    return b2sElements;
  }

  public void setB2sElements(int b2sElements) {
    this.b2sElements = b2sElements;
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
  
  public int getScores() {
    return scores;
  }
  public void setScores(int scores) {
    this.scores = scores;
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

  public DirectB2S toDirectB2S() {
    DirectB2S b2s = new DirectB2S();
    b2s.setName(this.getName());
    b2s.setFileName(this.getFilename());
    b2s.setEmulatorId(this.getEmulatorId());
    //b2s.setVpxAvailable(unknown but not needed);
    return b2s;
  }

}
