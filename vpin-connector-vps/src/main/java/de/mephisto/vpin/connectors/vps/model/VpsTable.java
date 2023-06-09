package de.mephisto.vpin.connectors.vps.model;

import java.util.List;

public class VpsTable {
  private String name;
  private List<String> features;
  private List<VpsAuthoredUrls> povFiles;
  private boolean broken;
  private List<VpsUrl> altColorFiles;
  private List<VpsUrl> altSoundFiles;
  private List<VpsAuthoredUrls> soundFiles;
  private List<VpsAuthoredUrls> romFiles;
  private List<VpsAuthoredUrls> pupPackFiles;
  private List<VpsTableFile> tableFiles;
  private List<VpsAuthoredUrls> topperFiles;
  private List<VpsAuthoredUrls> wheelArtFiles;

  private String ipdbUrl;
  private String manufacturer;
  private int players;
  private String type;
  private int year;
  private long updated;

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

  public long getUpdated() {
    return updated;
  }

  public void setUpdated(long updated) {
    this.updated = updated;
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

  public List<VpsUrl> getAltSoundFiles() {
    return altSoundFiles;
  }

  public void setAltSoundFiles(List<VpsUrl> altSoundFiles) {
    this.altSoundFiles = altSoundFiles;
  }

  public List<VpsUrl> getAltColorFiles() {
    return altColorFiles;
  }

  public void setAltColorFiles(List<VpsUrl> altColorFiles) {
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
}
