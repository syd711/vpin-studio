package de.mephisto.vpin.restclient.vpxz;

import de.mephisto.vpin.restclient.util.FileUtils;

import java.util.Objects;

public class VPXZFileInfo {
  private int files;
  private long size;
  private String fileName;

  public int getFiles() {
    return files;
  }

  public void setFiles(int files) {
    this.files = files;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    VPXZFileInfo that = (VPXZFileInfo) o;
    return files == that.files && size == that.size && Objects.equals(fileName, that.fileName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(files, size, fileName);
  }

  @Override
  public String toString() {
    if (files > 1) {
      return fileName + " (" + files + " files, " + FileUtils.readableFileSize(size) + ")";
    }

    if (size > 0) {
      return fileName + " (" + FileUtils.readableFileSize(size) + ")";
    }

    return fileName;
  }
}
