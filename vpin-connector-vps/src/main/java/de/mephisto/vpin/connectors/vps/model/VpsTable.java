package de.mephisto.vpin.connectors.vps.model;

import java.util.List;
import java.util.stream.Collectors;

public class VpsTable {
  private String id;
  private String name;
  private List<String> features;
  private List<VpsAuthoredUrls> povFiles;
  private boolean broken;
  private List<VpsAuthoredUrls> altColorFiles;
  private List<VpsAuthoredUrls> altSoundFiles;
  private List<VpsAuthoredUrls> soundFiles;
  private List<VpsAuthoredUrls> romFiles;
  private List<VpsAuthoredUrls> pupPackFiles;
  private List<VpsTableFile> tableFiles;
  private List<VpsAuthoredUrls> topperFiles;
  private List<VpsAuthoredUrls> wheelArtFiles;
  private List<VpsBackglassFile> b2sFiles;

  private String ipdbUrl;
  private String manufacturer;
  private int players;
  private String type;
  private int year;
  private long updatedAt;

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

  public List<VpsTableFile> getTableFiles() {
    return tableFiles;
  }

  public List<VpsTableFile> getTableFilesForFormat(String tableFormat) {
    return tableFiles.stream().filter(t -> t.getTableFormat() != null && t.getTableFormat().equals(tableFormat) && !(t.getUrls() == null || t.getUrls().isEmpty())).collect(Collectors.toList());
  }

  public void setTableFiles(List<VpsTableFile> tableFiles) {
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
}
