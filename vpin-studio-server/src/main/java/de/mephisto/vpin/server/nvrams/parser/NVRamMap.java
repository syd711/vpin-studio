package de.mephisto.vpin.server.nvrams.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class NVRamMap extends NVRamObject {

  /** The associated rom of this map */
  private String rom;
  private String romName;
  /** The associated nv file */
  private String nvramName;
  /** The relative path from root to the JSON file defintion */
  private String mapPath;

  @JsonProperty("_metadata")
  private NVRamMetadata metadata;

  @JsonProperty("_fileformat")
  private Double fileformat;

  // A descriptor (likely with a wpc_rtc encoding) with a date stamp of when PinMAME last saved the .nv file.
  @JsonProperty("last_played")
  private NVRamMapping lastPlayed;

  // An object of key/value pairs for groupings of adjustments, where the key describes the group (e.g. "A.1 Standard Adjustments")
  @JsonProperty("adjustments")
  private Map<String, NVRamMappings> adjustments;

  // Same as "adjustments", but for the game's audits (also referred to as "Bookkeeping").
  @JsonProperty("audits")
  private Map<String, NVRamMappings> audits;

  // A collection of memory areas used during a game to store the state of the game (e.g., player #, ball #, progressive jackpot value,...
  @JsonProperty("game_state")
  private NVRamGameState gameState;

  // A special section detailing DIP switch options for the game
  @JsonProperty("dip_switches")
  private Map<String, NVRamMapping> dipSwitches;

  // The traditional high score table that would usually start with the Grand Champion and then proceed through First Place to Fourth Place.
  @JsonProperty("high_scores")
  private List<NVRamScore> highScores;

  // Another array of descriptors with recognition of other in-game accomplishments.
  @JsonProperty("mode_champions")
  private List<NVRamScore> modeChampions;

  @JsonProperty("more_mode_champions")
  private List<NVRamScore> moreModeChampions;

  @JsonProperty("checksum8")
  private List<NVRamMapping> checksum8;

  @JsonProperty("checksum16")
  private List<NVRamMapping> checksum16;

  // yet to be decoded, do not use
  @JsonProperty("player_state")
  private Object playerState;

  @JsonProperty("limits")
  private Map<String, String> limits;
  

  //---------------------- Parsed mappings ----

  private NVRamPlatform ramPlatform;

  private List<ChecksumMapping> checksumEntries = new ArrayList<>();

  //-------------------------------------------------------

  public String getRom() {
    return rom;
  }

  public String getRomName() {
    return romName;
  }

  public void setRom(String rom, String romName) {
    this.rom = rom;
    this.romName = romName;
  }

  public String getNvramName() {
    return nvramName;
  }

  public void setNvramName(String nvramName) {
    this.nvramName = nvramName;
  }

  public String getMapPath() {
    return mapPath;
  }

  public void setMapPath(String mapPath) {
    this.mapPath = mapPath;
  }

  public NVRamPlatform getRamPlatform() {
    return ramPlatform;
  }

  public void setNVRamPlatform(NVRamPlatform ramPlatform) {
    this.ramPlatform = ramPlatform;
  }

  public boolean isBigEndian() {
		return ramPlatform != null ? !"little".equals(ramPlatform.getEndian()) : false;
  }


  @JsonIgnore
  public List<ChecksumMapping> getChecksumEntries() {
    return checksumEntries;
  }

  public void addChecksumEntry(ChecksumMapping mapping) {
    checksumEntries.add(mapping);
  }

  public NVRamRegion getMemoryArea(Integer address, String memType) {
		NVRamPlatform platform = getRamPlatform();
    List<NVRamRegion> layout = platform.getMemoryLayout();
    for (NVRamRegion region : layout) {
      if (address != null) {
        int start = BcdUtils.toInt(region.getAddress());
        int end = start + BcdUtils.toInt(region.getSize()) - 1;
        if (!(start <= address && address <= end)) continue;
      }
      if (memType != null && !memType.equals(region.getType())) continue;
      return region;
    }
    return null;
  }

  //-------------------------------------------
  // Getters for JSON properties

  public NVRamMetadata getMetadata() {
    return metadata;
  }

  public Double getFileformat() {
    return fileformat;
  }

  public NVRamMapping getLastPlayed() {
    return lastPlayed;
  }

  public NVRamGameState getGameState() {
    return gameState;
  }

  public Map<String, NVRamMapping> getDipSwitches() {
    return dipSwitches;
  }

  public Map<String, NVRamMappings> getAudits() {
    return audits;
  }

  public Map<String, NVRamMappings> getAdjustments() {
    return adjustments;
  }

  public List<NVRamScore> getHighScores() {
    return highScores;
  }

  public List<NVRamScore> getModeChampions() {
    return modeChampions;
  }

  public List<NVRamMapping> getChecksum8() {
    return checksum8;
  }

  public List<NVRamMapping> getChecksum16() {
    return checksum16;
  }
}
