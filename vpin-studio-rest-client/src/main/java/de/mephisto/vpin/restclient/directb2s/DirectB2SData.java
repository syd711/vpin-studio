package de.mephisto.vpin.restclient.directb2s;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DirectB2SData {
  private String filename;
  private int emulatorId;

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

  private int backgroundWidth;
  private int backgroundHeight;

  private int dmdWidth;
  private int dmdHeight;

  private int illuminations;

  private List<DirectB2SDataScore> scores;

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
  
  public List<DirectB2SDataScore> getScores() {
    return scores;
  }
  public void setScores(List<DirectB2SDataScore> scores) {
    this.scores = scores;
  }

  @JsonIgnore
  public int getNbScores() {
    return scores != null ? scores.size() : 0;
  }

  public void addScore(DirectB2SDataScore score) {
    if (scores == null) {
      scores = new ArrayList<>();
    }
    scores.add(score);
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

  public int getBackgroundWidth() {
    return backgroundWidth;
  }

  public void setBackgroundWidth(int backgroundWidth) {
    this.backgroundWidth = backgroundWidth;
  }

  public int getBackgroundHeight() {
    return backgroundHeight;
  }

  public void setBackgroundHeight(int backgroundHeight) {
    this.backgroundHeight = backgroundHeight;
  }

  public int getDmdWidth() {
    return dmdWidth;
  }

  public void setDmdWidth(int dmdWidth) {
    this.dmdWidth = dmdWidth;
  }

  public int getDmdHeight() {
    return dmdHeight;
  }

  public void setDmdHeight(int dmdHeight) {
    this.dmdHeight = dmdHeight;
  }

  public boolean isFullDmd() {
    return isFullDmd(dmdWidth, dmdHeight);
  }
  public static boolean isFullDmd(double imageWidth, double imageHeight) {
    double ratio = imageWidth / imageHeight;
    return ratio < 3.0;
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

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (object == null || getClass() != object.getClass()) return false;
    DirectB2SData that = (DirectB2SData) object;
    return emulatorId == that.emulatorId && tableType == that.tableType && numberOfPlayers == that.numberOfPlayers && grillHeight == that.grillHeight && b2sElements == that.b2sElements && filesize == that.filesize && backgroundAvailable == that.backgroundAvailable && dmdImageAvailable == that.dmdImageAvailable && illuminations == that.illuminations && scores == that.scores && Objects.equals(filename, that.filename) && Objects.equals(name, that.name) && Objects.equals(artwork, that.artwork) && Objects.equals(author, that.author) && Objects.equals(modificationDate, that.modificationDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(filename, emulatorId, name, tableType, numberOfPlayers, artwork, author, grillHeight, b2sElements, filesize, modificationDate, backgroundAvailable, dmdImageAvailable, illuminations, scores);
  }

  @Override
  public String toString() {
    return filename + " [" + emulatorId + "]";
  }

}
