package de.mephisto.vpin.connectors.assets;

import org.apache.commons.io.FilenameUtils;

import java.util.Objects;

public class TableAsset {
  private String name;
  private String url;
  private String sourceId;
  private String mimeType;
  private String author;
  private String screenSegment;
  private String emulator;
  private long length = -1;

  public long getLength() {
    return length;
  }

  public void setLength(long length) {
    this.length = length;
  }

  public String getEmulator() {
    return emulator;
  }

  public void setEmulator(String emulator) {
    this.emulator = emulator;
  }

  public String getScreen() {
    return screenSegment;
  }

  public void setScreen(String screenSegment) {
    this.screenSegment = screenSegment;
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
    if (emulator == null) {
      return name + "  [" + this.author + "]";
    }
    return name + "  [" + this.emulator + "/" + this.author + "]";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TableAsset that = (TableAsset) o;
    return Objects.equals(name, that.name) && Objects.equals(url, that.url) && Objects.equals(sourceId, that.sourceId) && Objects.equals(mimeType, that.mimeType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, url, sourceId, mimeType);
  }
}
