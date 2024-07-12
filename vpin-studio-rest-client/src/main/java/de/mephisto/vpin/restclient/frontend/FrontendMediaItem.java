package de.mephisto.vpin.restclient.frontend;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.util.MimeTypeUtil;

import java.io.File;

public class FrontendMediaItem {
  private String mimeType;
  private String uri;
  private final File file;
  private final VPinScreen screen;
  private int gameId;

  public FrontendMediaItem(int gameId, VPinScreen screen, File file) {
    this.file = file;
    this.gameId = gameId;
    this.screen = screen;
    this.uri = "media/" + gameId + "/" + screen.name();
    this.mimeType = MimeTypeUtil.determineMimeType(file);
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
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }
}
