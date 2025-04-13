package de.mephisto.vpin.restclient.textedit;

import java.io.File;
import java.util.Date;

public class TextFile {
  private VPinFile vPinFile;
  private String content;
  private Date lastModified;
  private String path;
  private int fileId;
  private int emulatorId;
  private long size;

  public TextFile(VPinFile vPinFile) {
    this.vPinFile = vPinFile;
  }

  public TextFile(File file) {
    this.path = file.getAbsolutePath();
    this.vPinFile = VPinFile.LOCAL;
  }

  public TextFile(String text) {
    this.content = text;
  }

  public TextFile() {

  }

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
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

  public int getFileId() {
    return fileId;
  }

  public void setFileId(int fileId) {
    this.fileId = fileId;
  }
}
