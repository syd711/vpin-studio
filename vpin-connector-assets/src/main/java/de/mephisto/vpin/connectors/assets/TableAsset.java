package de.mephisto.vpin.connectors.assets;

import org.apache.commons.io.FilenameUtils;

public class TableAsset {
  private String name;
  private String url;
  private String sourceId;
  private String mimeType;
  private String author;
  private String screen;
  private String emulator;

  public String getEmulator() {
    return emulator;
  }

  public void setEmulator(String emulator) {
    this.emulator = emulator;
  }

  public String getScreen() {
    return screen;
  }

  public void setScreen(String screen) {
    this.screen = screen;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getFileSuffix() {
    return FilenameUtils.getExtension(getName());
  }

  @Override
  public String toString() {
    return name + "  [" + this.emulator + "/" + this.author + "/]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TableAsset)) return false;

    TableAsset asset = (TableAsset) o;

    if (!name.equals(asset.name)) return false;
    return sourceId.equals(asset.sourceId);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + sourceId.hashCode();
    return result;
  }
}
