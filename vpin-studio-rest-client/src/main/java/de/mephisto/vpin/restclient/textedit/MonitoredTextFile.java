package de.mephisto.vpin.restclient.textedit;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.time.OffsetDateTime;

public class MonitoredTextFile {
  private MonitoredFile file;
  private String content;
  private OffsetDateTime lastModified;
  private String path;
  private String fileId;
  private int emulatorId;
  private long size;

  public MonitoredTextFile(MonitoredFile monitoredFile) {
    this.file = monitoredFile;
  }

  public MonitoredTextFile(File file) {
    this.path = file.getAbsolutePath();
    this.file = MonitoredFile.LOCAL_GAME_FILE;
  }

  public MonitoredTextFile(String text) {
    this.content = text;
  }

  public MonitoredTextFile() {

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

  public MonitoredFile getFile() {
    return file;
  }

  public void setFile(MonitoredFile file) {
    this.file = file;
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

  public OffsetDateTime getLastModified() {
    return lastModified;
  }

  public void setLastModified(OffsetDateTime lastModified) {
    this.lastModified = lastModified;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }
}
