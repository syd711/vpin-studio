package de.mephisto.vpin.restclient.archiving;

public class ArchivePackageInfo {
  public static final int TARGET_WHEEL_SIZE_WIDTH = 100;
  public final static String ARCHIVE_FILENAME = "package-info.json";

  private boolean directb2s;
  private boolean pupPack;
  private boolean rom;
  private boolean res;
  private boolean cfg;
  private boolean popperMedia;
  private boolean pov;
  private boolean music;
  private boolean altSound;
  private boolean altColor;
  private boolean highscore;
  private boolean flexDMD;
  private boolean ultraDMD;
  private boolean vpx;
  private boolean registryData;

  private String icon;
  private String thumbnail;

  public boolean isRegistryData() {
    return registryData;
  }

  public void setRegistryData(boolean registryData) {
    this.registryData = registryData;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }

  public boolean isRes() {
    return res;
  }

  public void setRes(boolean res) {
    this.res = res;
  }

  public boolean isCfg() {
    return cfg;
  }

  public void setCfg(boolean cfg) {
    this.cfg = cfg;
  }

  public boolean isAltColor() {
    return altColor;
  }

  public void setAltColor(boolean altColor) {
    this.altColor = altColor;
  }

  public boolean isVpx() {
    return vpx;
  }

  public void setVpx(boolean vpx) {
    this.vpx = vpx;
  }

  public boolean isDirectb2s() {
    return directb2s;
  }

  public void setDirectb2s(boolean directb2s) {
    this.directb2s = directb2s;
  }

  public boolean isPupPack() {
    return pupPack;
  }

  public void setPupPack(boolean pupPack) {
    this.pupPack = pupPack;
  }

  public boolean isRom() {
    return rom;
  }

  public void setRom(boolean rom) {
    this.rom = rom;
  }

  public boolean isPopperMedia() {
    return popperMedia;
  }

  public void setPopperMedia(boolean popperMedia) {
    this.popperMedia = popperMedia;
  }

  public boolean isPov() {
    return pov;
  }

  public void setPov(boolean pov) {
    this.pov = pov;
  }

  public boolean isMusic() {
    return music;
  }

  public void setMusic(boolean music) {
    this.music = music;
  }

  public boolean isAltSound() {
    return altSound;
  }

  public void setAltSound(boolean altSound) {
    this.altSound = altSound;
  }

  public boolean isHighscore() {
    return highscore;
  }

  public void setHighscore(boolean highscore) {
    this.highscore = highscore;
  }

  public boolean isFlexDMD() {
    return flexDMD;
  }

  public void setFlexDMD(boolean flexDMD) {
    this.flexDMD = flexDMD;
  }

  public boolean isUltraDMD() {
    return ultraDMD;
  }

  public void setUltraDMD(boolean ultraDMD) {
    this.ultraDMD = ultraDMD;
  }
}
