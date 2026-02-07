package de.mephisto.vpin.restclient.vpxz;


import java.util.Objects;

public class VPXZPackageInfo {
  public static final int TARGET_WHEEL_SIZE_WIDTH = 100;
  public final static String PACKAGE_INFO_JSON_FILENAME = "package-info.json";
  public final static String REGISTRY_FILENAME = "registry.json";

  private VPXZFileInfo directb2s;
  private VPXZFileInfo rom;
  private VPXZFileInfo nvRam;
  private VPXZFileInfo res;
  private VPXZFileInfo vbs;
  private VPXZFileInfo pov;
  private VPXZFileInfo ini;
  private VPXZFileInfo cfg;
  private VPXZFileInfo music;
  private VPXZFileInfo altSound;
  private VPXZFileInfo altColor;
  private VPXZFileInfo highscore;
  private VPXZFileInfo dmd;
  private VPXZFileInfo vpx;
  private VPXZFileInfo mameData;

  private String thumbnail;

  public VPXZFileInfo getCfg() {
    return cfg;
  }

  public void setCfg(VPXZFileInfo cfg) {
    this.cfg = cfg;
  }

  public VPXZFileInfo getDirectb2s() {
    return directb2s;
  }

  public void setDirectb2s(VPXZFileInfo directb2s) {
    this.directb2s = directb2s;
  }

  public VPXZFileInfo getRom() {
    return rom;
  }

  public void setRom(VPXZFileInfo rom) {
    this.rom = rom;
  }

  public VPXZFileInfo getNvRam() {
    return nvRam;
  }

  public void setNvRam(VPXZFileInfo nvRam) {
    this.nvRam = nvRam;
  }

  public VPXZFileInfo getRes() {
    return res;
  }

  public void setRes(VPXZFileInfo res) {
    this.res = res;
  }

  public VPXZFileInfo getVbs() {
    return vbs;
  }

  public void setVbs(VPXZFileInfo vbs) {
    this.vbs = vbs;
  }

  public VPXZFileInfo getPov() {
    return pov;
  }

  public void setPov(VPXZFileInfo pov) {
    this.pov = pov;
  }

  public VPXZFileInfo getIni() {
    return ini;
  }

  public void setIni(VPXZFileInfo ini) {
    this.ini = ini;
  }

  public VPXZFileInfo getMusic() {
    return music;
  }

  public void setMusic(VPXZFileInfo music) {
    this.music = music;
  }

  public VPXZFileInfo getAltSound() {
    return altSound;
  }

  public void setAltSound(VPXZFileInfo altSound) {
    this.altSound = altSound;
  }

  public VPXZFileInfo getAltColor() {
    return altColor;
  }

  public void setAltColor(VPXZFileInfo altColor) {
    this.altColor = altColor;
  }

  public VPXZFileInfo getHighscore() {
    return highscore;
  }

  public void setHighscore(VPXZFileInfo highscore) {
    this.highscore = highscore;
  }

  public VPXZFileInfo getDmd() {
    return dmd;
  }

  public void setDmd(VPXZFileInfo dmd) {
    this.dmd = dmd;
  }

  public VPXZFileInfo getVpx() {
    return vpx;
  }

  public void setVpx(VPXZFileInfo vpx) {
    this.vpx = vpx;
  }

  public VPXZFileInfo getMameData() {
    return mameData;
  }

  public void setMameData(VPXZFileInfo mameData) {
    this.mameData = mameData;
  }

  public String getThumbnail() {
    return thumbnail;
  }

  public void setThumbnail(String thumbnail) {
    this.thumbnail = thumbnail;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    VPXZPackageInfo that = (VPXZPackageInfo) o;
    return Objects.equals(directb2s, that.directb2s) && Objects.equals(rom, that.rom) && Objects.equals(nvRam, that.nvRam) && Objects.equals(res, that.res) && Objects.equals(vbs, that.vbs) && Objects.equals(pov, that.pov) && Objects.equals(ini, that.ini) && Objects.equals(cfg, that.cfg) && Objects.equals(music, that.music) && Objects.equals(altSound, that.altSound) && Objects.equals(altColor, that.altColor) && Objects.equals(highscore, that.highscore) && Objects.equals(dmd, that.dmd) && Objects.equals(vpx, that.vpx) && Objects.equals(mameData, that.mameData) && Objects.equals(thumbnail, that.thumbnail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(directb2s, rom, nvRam, res, vbs,  pov, ini, cfg, music, altSound, altColor, highscore, dmd, vpx, mameData, thumbnail);
  }
}
