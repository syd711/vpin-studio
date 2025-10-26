package de.mephisto.vpin.restclient.backups;

import java.util.Objects;

public class BackupPackageInfo {
  public static final int TARGET_WHEEL_SIZE_WIDTH = 100;
  public final static String PACKAGE_INFO_JSON_FILENAME = "package-info.json";
  public final static String REGISTRY_FILENAME = "registry.json";

  private BackupFileInfo directb2s;
  private BackupFileInfo pupPack;
  private BackupFileInfo rom;
  private BackupFileInfo nvRam;
  private BackupFileInfo res;
  private BackupFileInfo vbs;
  private BackupFileInfo popperMedia;
  private BackupFileInfo pov;
  private BackupFileInfo ini;
  private BackupFileInfo cfg;
  private BackupFileInfo music;
  private BackupFileInfo altSound;
  private BackupFileInfo altColor;
  private BackupFileInfo highscore;
  private BackupFileInfo dmd;
  private BackupFileInfo vpx;
  private BackupFileInfo mameData;

  private String thumbnail;

  public BackupFileInfo getCfg() {
    return cfg;
  }

  public void setCfg(BackupFileInfo cfg) {
    this.cfg = cfg;
  }

  public BackupFileInfo getDirectb2s() {
    return directb2s;
  }

  public void setDirectb2s(BackupFileInfo directb2s) {
    this.directb2s = directb2s;
  }

  public BackupFileInfo getPupPack() {
    return pupPack;
  }

  public void setPupPack(BackupFileInfo pupPack) {
    this.pupPack = pupPack;
  }

  public BackupFileInfo getRom() {
    return rom;
  }

  public void setRom(BackupFileInfo rom) {
    this.rom = rom;
  }

  public BackupFileInfo getNvRam() {
    return nvRam;
  }

  public void setNvRam(BackupFileInfo nvRam) {
    this.nvRam = nvRam;
  }

  public BackupFileInfo getRes() {
    return res;
  }

  public void setRes(BackupFileInfo res) {
    this.res = res;
  }

  public BackupFileInfo getVbs() {
    return vbs;
  }

  public void setVbs(BackupFileInfo vbs) {
    this.vbs = vbs;
  }

  public BackupFileInfo getPopperMedia() {
    return popperMedia;
  }

  public void setPopperMedia(BackupFileInfo popperMedia) {
    this.popperMedia = popperMedia;
  }

  public BackupFileInfo getPov() {
    return pov;
  }

  public void setPov(BackupFileInfo pov) {
    this.pov = pov;
  }

  public BackupFileInfo getIni() {
    return ini;
  }

  public void setIni(BackupFileInfo ini) {
    this.ini = ini;
  }

  public BackupFileInfo getMusic() {
    return music;
  }

  public void setMusic(BackupFileInfo music) {
    this.music = music;
  }

  public BackupFileInfo getAltSound() {
    return altSound;
  }

  public void setAltSound(BackupFileInfo altSound) {
    this.altSound = altSound;
  }

  public BackupFileInfo getAltColor() {
    return altColor;
  }

  public void setAltColor(BackupFileInfo altColor) {
    this.altColor = altColor;
  }

  public BackupFileInfo getHighscore() {
    return highscore;
  }

  public void setHighscore(BackupFileInfo highscore) {
    this.highscore = highscore;
  }

  public BackupFileInfo getDmd() {
    return dmd;
  }

  public void setDmd(BackupFileInfo dmd) {
    this.dmd = dmd;
  }

  public BackupFileInfo getVpx() {
    return vpx;
  }

  public void setVpx(BackupFileInfo vpx) {
    this.vpx = vpx;
  }

  public BackupFileInfo getMameData() {
    return mameData;
  }

  public void setMameData(BackupFileInfo mameData) {
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
    BackupPackageInfo that = (BackupPackageInfo) o;
    return Objects.equals(directb2s, that.directb2s) && Objects.equals(pupPack, that.pupPack) && Objects.equals(rom, that.rom) && Objects.equals(nvRam, that.nvRam) && Objects.equals(res, that.res) && Objects.equals(vbs, that.vbs) && Objects.equals(popperMedia, that.popperMedia) && Objects.equals(pov, that.pov) && Objects.equals(ini, that.ini) && Objects.equals(cfg, that.cfg) && Objects.equals(music, that.music) && Objects.equals(altSound, that.altSound) && Objects.equals(altColor, that.altColor) && Objects.equals(highscore, that.highscore) && Objects.equals(dmd, that.dmd) && Objects.equals(vpx, that.vpx) && Objects.equals(mameData, that.mameData) && Objects.equals(thumbnail, that.thumbnail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(directb2s, pupPack, rom, nvRam, res, vbs, popperMedia, pov, ini, cfg, music, altSound, altColor, highscore, dmd, vpx, mameData, thumbnail);
  }
}
