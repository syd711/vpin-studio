package de.mephisto.vpin.server.nvrams.parser;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties("copyright")
public class NVRamMetadata extends NVRamObject {

  // A `float` indicating the map's version. (*)
  @JsonProperty("version")
  private Double version;

  @JsonProperty("license")
  private String license;

  @JsonProperty("endian")
  private String endian;

  @JsonProperty("ram_size")
  private String ramsize;

  // Identifies the hardware platform (e.g., Williams WPC) for  the ROMs covered by this map.
  // This is a string that corresponds to a JSON file in the top-level platforms/ directory
  @JsonProperty("platform")
  private String platform;

  // A list of PinMAME ROMs that use this map version (*)
  @JsonProperty("roms")
  private String[] roms;

  // A list of PinMAME ROMs that only support "home" use
  @JsonProperty("free_only")
  private String[] freeOnly;

  // Characters to use for the `ch` encoding instead of a straight  ASCII table.  See Whirlwind (whirl_l3.map.json) as an example.
  @JsonProperty("char_map")
  private String charMap;

  // Dictionary for value lists used by multiple entries. See maps/gottlieb/system80b/80b-8digit-C-8KB.map.json
  // Added to support long lists of pricing options that apply to multiple sets of DIP switches
  @JsonProperty("values")
  private Map<String, List<Object>> values;   

  //------------------------------------------

  public Double getVersion() {
    return version;
  }

  public String getLicense() {
    return license;
  }

  public String getEndian() {
    return endian;
  }

  public String getRamSize() {
    return ramsize;
  }

  public String getPlatform() {
    return platform;
  }

  public String[] getRoms() {
    return roms;
  }

  public String[] getFreeOnly() {
    return freeOnly;
  }

  public String getCharMap() {
    return charMap;
  }

  public Map<String, List<Object>> getValues() {
    return values;
  }
}
