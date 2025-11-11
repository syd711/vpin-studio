package de.mephisto.vpin.restclient.games;

import java.util.Date;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

public class FrontendMediaItemRepresentation {
  private String mimeType;
  private String uri;
  private VPinScreen screen;
  private int gameId;
  private String name;
  private Date modificationDate;
  private long size;

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public Date getModificationDate() {
    return modificationDate;
  }

  public void setModificationDate(Date modificationDate) {
    this.modificationDate = modificationDate;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public VPinScreen getScreen() {
    return screen;
  }

  public void setScreen(VPinScreen screen) {
    this.screen = screen;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
