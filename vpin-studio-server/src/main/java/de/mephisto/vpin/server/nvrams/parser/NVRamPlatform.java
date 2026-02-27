package de.mephisto.vpin.server.nvrams.parser;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NVRamPlatform extends NVRamObject {

  @JsonProperty("name")
  private String name;
  @JsonProperty("cpu")
  private String cpu;
  @JsonProperty("endian")
  private String endian;

  @JsonProperty("memory_layout")
  private List<NVRamRegion> regions = new ArrayList<>();


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCpu() {
    return cpu;
  }

  public void setCpu(String cpu) {
    this.cpu = cpu;
  }

  public String getEndian() {
    return endian;
  }

  public void setEndian(String endian) {
    this.endian = endian;
  }

  public List<NVRamRegion> getMemoryLayout() {
    return regions;
  }

  public void addLayout(NVRamRegion region) {
    regions.add(region);
  }
}
