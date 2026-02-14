package de.mephisto.vpin.restclient.frontend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.util.MimeTypeUtil;

import java.io.File;
import java.util.Date;

public class FrontendMediaItem {
  private String mimeType;
  private final File file;
  private final VPinScreen screen;
  private Date modificationDate;
  private long size;
  private String uri;

  public static FrontendMediaItem forGame(int gameId, VPinScreen screen, File file) {
    return new FrontendMediaItem(screen, file, "media" + "/" + gameId + "/" + screen);
  }
  public static FrontendMediaItem forPlaylist(int playlistId, VPinScreen screen, File file) {
    return new FrontendMediaItem(screen, file, "playlistmedia" + "/" + playlistId + "/" + screen);
  }

  private FrontendMediaItem(VPinScreen screen, File file, String uri) {
    this.file = file;
    this.screen = screen;
    this.mimeType = MimeTypeUtil.determineMimeType(file);
    this.modificationDate = new Date(file.lastModified());
    this.size = file.length();
    this.uri = uri;
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

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
}
