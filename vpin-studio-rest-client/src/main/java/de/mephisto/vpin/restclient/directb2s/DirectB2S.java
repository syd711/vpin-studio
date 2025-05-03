package de.mephisto.vpin.restclient.directb2s;

import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.mephisto.vpin.restclient.util.FileUtils;

public class DirectB2S {

  private int emulatorId;
  private String fileName;

  private int gameId;

  public String getName() {
    return FilenameUtils.getBaseName(fileName);
  }

  public String getFileName() {
    return fileName;
  }

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
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

    DirectB2S that = (DirectB2S) o;
    if (this.emulatorId != that.emulatorId) return false;
    return Objects.equals(this.fileName, that.fileName);
  }

  @Override
  public int hashCode() {
    int result = fileName != null ? fileName.hashCode() : 0;
    result = 31 * result + emulatorId;
    return result;
  }

  //----------------------------------------- VERSIONS MANAGEMENT ---

  private boolean enabled = false;

  private List<String> versions = new ArrayList<>();

  public List<String> getVersions() {
    // prevent caller from modifying the model
    return Collections.unmodifiableList(versions);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void addVersion(String fileName) {
    this.fileName = FileUtils.fromUniqueFile(fileName);

    // make sure main file is always the first version
    boolean main = StringUtils.equalsIgnoreCase(this.fileName, fileName);
    if (main) {
      this.versions.add(0, fileName);
    }
    else {
      this.versions.add(fileName);
    }
    // backglass is enabled if it contains a main one
    this.enabled |= main;
  }

  public void clearVersions() {
    this.fileName = null;
    this.versions.clear();
    this.enabled = false;
  }

  @JsonIgnore
  public String getVersion(int i) {
    return i < versions.size() ? versions.get(i) : null;
  }

  @JsonIgnore
  public int getNbVersions() {
    return versions != null ? versions.size() : 0;
  }
}
