package de.mephisto.vpin.restclient.textedit;

import java.util.Date;

public class TextFile {
  private VPinFile vPinFile;
  private String content;
  private Date lastModified;
  private String path;
  private String fileId;
  private long size;

  public TextFile(VPinFile vPinFile) {
    this.vPinFile = vPinFile;
  }

  public TextFile() {

  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public VPinFile getvPinFile() {
    return vPinFile;
  }

  public void setvPinFile(VPinFile vPinFile) {
    this.vPinFile = vPinFile;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }
}
