package de.mephisto.vpin.restclient.textedit;

import java.io.File;
import java.time.OffsetDateTime;

public class TextEditorFile {
  private TextEditorFileTypes file;
  private String content;
  private OffsetDateTime lastModified;
  private String path;
  private String fileId;
  private int emulatorId;
  private long size;

  public TextEditorFile(TextEditorFileTypes textEditorFileTypes) {
    this.file = textEditorFileTypes;
  }

  public TextEditorFile(File file) {
    this.path = file.getAbsolutePath();
    this.file = TextEditorFileTypes.LOCAL_GAME_FILE;
  }

  public TextEditorFile(String text) {
    this.content = text;
  }

  public TextEditorFile() {

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

  public TextEditorFileTypes getFile() {
    return file;
  }

  public void setFile(TextEditorFileTypes file) {
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
