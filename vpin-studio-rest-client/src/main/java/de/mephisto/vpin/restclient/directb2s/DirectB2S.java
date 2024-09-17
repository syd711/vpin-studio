package de.mephisto.vpin.restclient.directb2s;

import java.util.Objects;

import org.apache.commons.io.FilenameUtils;

public class DirectB2S {
  private int emulatorId;
  private boolean vpxAvailable;
  private String fileName;

  public String getName() {
    return FilenameUtils.getBaseName(fileName);
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public boolean isVpxAvailable() {
    return vpxAvailable;
  }

  public void setVpxAvailable(boolean vpxAvailable) {
    this.vpxAvailable = vpxAvailable;
  }

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DirectB2S)) return false;

    DirectB2S directB2S = (DirectB2S) o;

    if (emulatorId != directB2S.emulatorId) return false;
    return Objects.equals(fileName, directB2S.fileName);
  }

  @Override
  public int hashCode() {
    int result = fileName != null ? fileName.hashCode() : 0;
    result = 31 * result + emulatorId;
    return result;
  }
}
