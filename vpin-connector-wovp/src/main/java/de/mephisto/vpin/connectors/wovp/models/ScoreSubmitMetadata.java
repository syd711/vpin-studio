package de.mephisto.vpin.connectors.wovp.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ScoreSubmitMetadata {

  @JsonProperty("vpin-studio-version")
  private String vpinStudioVersion;

  @JsonProperty("vpx-file")
  private String vpxFile;

  private String rom;

  public String getVpinStudioVersion() {
    return vpinStudioVersion;
  }

  public void setVpinStudioVersion(String vpinStudioVersion) {
    this.vpinStudioVersion = vpinStudioVersion;
  }

  public String getVpxFile() {
    return vpxFile;
  }

  public void setVpxFile(String vpxFile) {
    this.vpxFile = vpxFile;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }
}
