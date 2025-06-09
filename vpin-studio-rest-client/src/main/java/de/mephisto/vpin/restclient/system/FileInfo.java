package de.mephisto.vpin.restclient.system;

import java.io.File;

public class FileInfo {

  private boolean isFile;

  private File file;

  private File fallback;

  /**
   * Simple factory
   */
  public static FileInfo file(File file, File fallback) {
    FileInfo info = new FileInfo();
    info.isFile = true;
    info.setFile(file);
    info.setFallback(fallback);
    return info;
  }

  public static FileInfo folder(File folder, File fallback) {
    FileInfo info = new FileInfo();
    info.isFile = false;
    info.setFile(folder);
    info.setFallback(fallback);
    return info;
  }

  public boolean isFile() {
    return isFile;
  }

  public File getFile() {
    return file;
  }

  public void setFile(File folder) {
    this.file = folder;
  }

  public File getFallback() {
    return fallback;
  }

  public void setFallback(File fallback) {
    this.fallback = fallback;
  }
}
