package de.mephisto.vpin.restclient.archiving;

public class ArchivePackageInfo {
  public static final int TARGET_WHEEL_SIZE_WIDTH = 100;
  public final static String ARCHIVE_FILENAME = "package-info.json";
  public final static String REGISTRY_FILENAME = "registry.json";

  private ArchiveFileInfo directb2s;
  private ArchiveFileInfo pupPack;
  private ArchiveFileInfo rom;
  private ArchiveFileInfo nvRam;
  private ArchiveFileInfo res;
  private ArchiveFileInfo vbs;
  private ArchiveFileInfo popperMedia;
  private ArchiveFileInfo pov;
  private ArchiveFileInfo ini;
  private ArchiveFileInfo music;
  private ArchiveFileInfo altSound;
  private ArchiveFileInfo altColor;
  private ArchiveFileInfo highscore;
  private ArchiveFileInfo dmd;
  private ArchiveFileInfo vpx;
  private ArchiveFileInfo mameData;

  private String icon;
  private String thumbnail;

  public ArchiveFileInfo getDirectb2s() {
    return directb2s;
  }

  public void setDirectb2s(ArchiveFileInfo directb2s) {
    this.directb2s = directb2s;
  }

  public ArchiveFileInfo getPupPack() {
    return pupPack;
  }

  public void setPupPack(ArchiveFileInfo pupPack) {
    this.pupPack = pupPack;
  }

  public ArchiveFileInfo getRom() {
    return rom;
  }

  public void setRom(ArchiveFileInfo rom) {
    this.rom = rom;
  }

  public ArchiveFileInfo getNvRam() {
    return nvRam;
  }

  public void setNvRam(ArchiveFileInfo nvRam) {
    this.nvRam = nvRam;
  }

  public ArchiveFileInfo getRes() {
    return res;
  }

  public void setRes(ArchiveFileInfo res) {
    this.res = res;
  }

  public ArchiveFileInfo getVbs() {
    return vbs;
  }

  public void setVbs(ArchiveFileInfo vbs) {
    this.vbs = vbs;
  }

  public ArchiveFileInfo getPopperMedia() {
    return popperMedia;
  }

  public void setPopperMedia(ArchiveFileInfo popperMedia) {
    this.popperMedia = popperMedia;
  }

  public ArchiveFileInfo getPov() {
    return pov;
  }

  public void setPov(ArchiveFileInfo pov) {
    this.pov = pov;
  }

  public ArchiveFileInfo getIni() {
    return ini;
  }

  public void setIni(ArchiveFileInfo ini) {
    this.ini = ini;
  }

  public ArchiveFileInfo getMusic() {
    return music;
  }

  public void setMusic(ArchiveFileInfo music) {
    this.music = music;
  }

  public ArchiveFileInfo getAltSound() {
    return altSound;
  }

  public void setAltSound(ArchiveFileInfo altSound) {
    this.altSound = altSound;
  }

  public ArchiveFileInfo getAltColor() {
    return altColor;
  }

  public void setAltColor(ArchiveFileInfo altColor) {
    this.altColor = altColor;
  }

  public ArchiveFileInfo getHighscore() {
    return highscore;
  }

  public void setHighscore(ArchiveFileInfo highscore) {
    this.highscore = highscore;
  }

  public ArchiveFileInfo getDmd() {
    return dmd;
  }

  public void setDmd(ArchiveFileInfo dmd) {
    this.dmd = dmd;
  }

  public ArchiveFileInfo getVpx() {
    return vpx;
  }

  public void setVpx(ArchiveFileInfo vpx) {
    this.vpx = vpx;
  }

  public ArchiveFileInfo getMameData() {
    return mameData;
  }

  public void setMameData(ArchiveFileInfo mameData) {
    this.mameData = mameData;
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
}
