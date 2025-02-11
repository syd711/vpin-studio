package de.mephisto.vpin.restclient.directb2s;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import de.mephisto.vpin.restclient.util.FileUtils;

public class DirectB2SAndVersions { //extends DirectB2S {

  private int emulatorId;
  private boolean gameAvailable;
  private String fileName;

  public String getName() {
    return FilenameUtils.getBaseName(fileName);
  }

  public String getFileName() {
    return fileName;
  }

  public void _setFileName(String fileName) {
    this.fileName = fileName;
  }

  public boolean isGameAvailable() {
    return gameAvailable;
  }

  public void setGameAvailable(boolean vpxAvailable) {
    this.gameAvailable = vpxAvailable;
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
    if (!(o instanceof DirectB2SAndVersions)) return false;

    DirectB2SAndVersions that = (DirectB2SAndVersions) o;
    if (this.emulatorId != that.emulatorId) return false;
    return Objects.equals(this.fileName, that.fileName);
  }

  @Override
  public int hashCode() {
    int result = fileName != null ? fileName.hashCode() : 0;
    result = 31 * result + emulatorId;
    return result;
  }

  //-----------------------------------------

  private boolean enabled = false;

  private List<String> versions = new ArrayList<>();

  public void addFileName(String fileName) {
    String mainFileName = FileUtils.fromUniqueFile(fileName);
    _setFileName(mainFileName);
    
    // make sure main file is always the first version
    boolean main = StringUtils.equalsIgnoreCase(mainFileName, fileName);
    if (main) {
      this.versions.add(0, fileName);
    }
    else {
      this.versions.add(fileName);
    }
    // backglass is enabled if it contains a main one
    this.enabled |= main;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getVersion(int i) {
    return i < versions.size() ? versions.get(i) : null;
  }

  public List<String> getVersions() {
    return versions;
  }

  public int getNbVersions() {
    return versions != null ? versions.size() : 0;
  }

  public void setVersions(List<String> versions) {
    this.versions = versions;
  }

  /**
   * Add all versions of directB2S into this
   */
  public void merge(DirectB2SAndVersions directB2S) {
    if (directB2S.isEnabled()) {
      this.versions.addAll(0, directB2S.versions);
    }
    else {
      this.versions.addAll(directB2S.versions);
    }
    this.enabled |= directB2S.enabled;
  }
}
