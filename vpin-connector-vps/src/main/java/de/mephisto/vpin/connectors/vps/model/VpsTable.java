package de.mephisto.vpin.connectors.vps.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class VpsTable {
  private String id;
  private String name;
  private List<String> features;
  private List<String> designers;
  private List<String> theme;
  private List<VpsAuthoredUrls> povFiles;
  private boolean broken;
  private List<VpsAuthoredUrls> altColorFiles;
  private List<VpsAuthoredUrls> altSoundFiles;
  private List<VpsAuthoredUrls> soundFiles;
  private List<VpsAuthoredUrls> romFiles;
  private List<VpsAuthoredUrls> pupPackFiles;
  private List<VpsTableVersion> tableFiles;
  private List<VpsAuthoredUrls> topperFiles;
  private List<VpsAuthoredUrls> wheelArtFiles;
  private List<VpsBackglassFile> b2sFiles;
  private List<VpsTutorialUrls> tutorialFiles;

  private String ipdbUrl;
  private String manufacturer;
  private int players;
  private String type;
  private int year;
  private long updatedAt;

  public List<VpsTutorialUrls> getTutorialFiles() {
    return tutorialFiles;
  }

  public void setTutorialFiles(List<VpsTutorialUrls> tutorialFiles) {
    this.tutorialFiles = tutorialFiles;
  }

  public List<String> getDesigners() {
    return designers;
  }

  public void setDesigners(List<String> designers) {
    this.designers = designers;
  }

  public List<String> getTheme() {
    return theme;
  }

  public void setTheme(List<String> theme) {
    this.theme = theme;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getIpdbUrl() {
    return ipdbUrl;
  }

  public void setIpdbUrl(String ipdbUrl) {
    this.ipdbUrl = ipdbUrl;
  }

  public String getManufacturer() {
    return manufacturer;
  }

  public void setManufacturer(String manufacturer) {
    this.manufacturer = manufacturer;
  }

  public List<VpsBackglassFile> getB2sFiles() {
    return b2sFiles;
  }

  public void setB2sFiles(List<VpsBackglassFile> b2sFiles) {
    this.b2sFiles = b2sFiles;
  }

  public int getPlayers() {
    return players;
  }

  public void setPlayers(int players) {
    this.players = players;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public long getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(long updatedAt) {
    this.updatedAt = updatedAt;
  }

  public List<VpsAuthoredUrls> getWheelArtFiles() {
    return wheelArtFiles;
  }

  public void setWheelArtFiles(List<VpsAuthoredUrls> wheelArtFiles) {
    this.wheelArtFiles = wheelArtFiles;
  }

  public List<VpsAuthoredUrls> getTopperFiles() {
    return topperFiles;
  }

  public void setTopperFiles(List<VpsAuthoredUrls> topperFiles) {
    this.topperFiles = topperFiles;
  }

  public List<VpsTableVersion> getTableFiles() {
    return tableFiles;
  }

  public List<VpsTableVersion> getTableFilesForFormat(String tableFormat) {
    return tableFiles.stream().filter(t -> isValidTableVersion(t, tableFormat)).collect(Collectors.toList());
  }

  private boolean isValidTableVersion(VpsTableVersion t, String tableFormat) {
    if(t.getTableFormat() == null) {
      return true;
    }
    return t.getTableFormat() != null && t.getTableFormat().equals(tableFormat) && !(t.getUrls() == null || t.getUrls().isEmpty());
  }

  public void setTableFiles(List<VpsTableVersion> tableFiles) {
    Collections.sort(tableFiles, Comparator.comparingLong((VpsTableVersion o) -> o.getUpdatedAt()));
    Collections.reverse(tableFiles);
    this.tableFiles = tableFiles;
  }

  public List<VpsAuthoredUrls> getPupPackFiles() {
    return pupPackFiles;
  }

  public void setPupPackFiles(List<VpsAuthoredUrls> pupPackFiles) {
    this.pupPackFiles = pupPackFiles;
  }

  public List<VpsAuthoredUrls> getRomFiles() {
    return romFiles;
  }

  public void setRomFiles(List<VpsAuthoredUrls> romFiles) {
    this.romFiles = romFiles;
  }

  public List<VpsAuthoredUrls> getSoundFiles() {
    return soundFiles;
  }

  public void setSoundFiles(List<VpsAuthoredUrls> soundFiles) {
    this.soundFiles = soundFiles;
  }

  public List<VpsAuthoredUrls> getAltSoundFiles() {
    return altSoundFiles;
  }

  public void setAltSoundFiles(List<VpsAuthoredUrls> altSoundFiles) {
    this.altSoundFiles = altSoundFiles;
  }

  public List<VpsAuthoredUrls> getAltColorFiles() {
    return altColorFiles;
  }

  public void setAltColorFiles(List<VpsAuthoredUrls> altColorFiles) {
    this.altColorFiles = altColorFiles;
  }

  public boolean isBroken() {
    return broken;
  }

  public void setBroken(boolean broken) {
    this.broken = broken;
  }

  public String getName() {
    return name;
  }

  public String getDisplayName() {
    String result = this.name;
    if(this.manufacturer != null && this.manufacturer.length() >0) {
      result = result + " | " + this.manufacturer;
    }

    if(this.year > 0) {
      result = result + " (" + this.year + ")";
    }

    return result;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getFeatures() {
    return features;
  }

  public void setFeatures(List<String> features) {
    this.features = features;
  }

  public List<VpsAuthoredUrls> getPovFiles() {
    return povFiles;
  }

  public void setPovFiles(List<VpsAuthoredUrls> povFiles) {
    this.povFiles = povFiles;
  }

  @Override
  public String toString() {
    return this.getName();
  }

  public VpsTableVersion getVersion(String extTableVersionId) {
    if (this.tableFiles != null) {
      for (VpsTableVersion tableFile : this.tableFiles) {
        if (tableFile.getId().equalsIgnoreCase(extTableVersionId)) {
          return tableFile;
        }
      }
    }
    return null;
  }
}
