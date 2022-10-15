package de.mephisto.vpin.restclient.representations;

public class GameMediaItemRepresentation {
  private String mimeType;
  private String uri;

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
}
