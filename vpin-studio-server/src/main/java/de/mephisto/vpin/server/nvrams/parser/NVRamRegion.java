package de.mephisto.vpin.server.nvrams.parser;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NVRamRegion extends NVRamObject {

  @JsonProperty("label")
  private String label;

  private Integer address;

  @JsonProperty("address")
  private void unpackAddress(Object address) {
    this.address = address != null ? BcdUtils.toInt(address) : 0;
  }

  private Integer size;

  @JsonProperty("size")
  private void unpackSize(Object size) {
    this.size = size != null ? BcdUtils.toInt(size) : null;
  }

  @JsonProperty("type")
  private String type;

  @JsonProperty("nibble")
  private Nibble nibble = Nibble.BOTH;

  //----------------------------------------------

  public static NVRamRegion createDefault(String label, int size) {
    NVRamRegion region = new NVRamRegion();
    region.label = label;
    region.address = 0;
    region.size = size;
    region.type = "nvram";
    region.nibble = Nibble.BOTH;
    return region;
  }

  public String getLabel() {
    return label;
  }

  public Integer getAddress() {
    return address;
  }

  public Integer getSize() {
    return size;
  }

  public String getType() {
    return type;
  }

  public Nibble getNibble() {
    return nibble;
  }
}
