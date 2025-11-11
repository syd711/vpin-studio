package de.mephisto.vpin.restclient.frontend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.util.MimeTypeUtil;

import java.io.File;
import java.util.Date;

public class FrontendMediaItem {
  private String mimeType;
  private final File file;
  private final VPinScreen screen;
  private int gameId;
  private Date modificationDate;
  private long size;

  public FrontendMediaItem(int gameId, VPinScreen screen, File file) {
    this.file = file;
    this.gameId = gameId;
    this.screen = screen;
    this.mimeType = MimeTypeUtil.determineMimeType(file);
    this.modificationDate = new Date(file.lastModified());
    this.size = file.length();
  }

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

  @JsonIgnore
  public File getFile() {
    return file;
  }

  public VPinScreen getScreen() {
    return screen;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getName() {
    return this.file.getName();
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public String getUri() {
    return "media/" + getGameId() + "/" + getScreen();
  }
}
