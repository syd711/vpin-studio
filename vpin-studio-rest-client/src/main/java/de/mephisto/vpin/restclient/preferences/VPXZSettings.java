package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;

public class VPXZSettings extends JsonSettings {
  private boolean directb2s = true;
  private boolean rom = true;
  private boolean nvRam = true;
  private boolean res = true;
  private boolean vbs = true;
  private boolean pov = true;
  private boolean ini = true;
  private boolean music = true;
  private boolean altSound = true;
  private boolean altColor = true;
  private boolean highscore = true;
  private boolean dmd = true;
  private boolean vpx = true;
  private boolean b2sSettings = true;
  private boolean dmdDeviceData = true;

  private boolean isOverwriteFile = true;

  private boolean enabled;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isDmdDeviceData() {
    return dmdDeviceData;
  }

  public void setDmdDeviceData(boolean dmdDeviceData) {
    this.dmdDeviceData = dmdDeviceData;
  }

  public boolean isOverwriteFile() {
    return isOverwriteFile;
  }

  public void setOverwriteFile(boolean overwriteFile) {
    this.isOverwriteFile = overwriteFile;
  }

  public boolean isB2sSettings() {
    return b2sSettings;
  }

  public void setB2sSettings(boolean b2sSettings) {
    this.b2sSettings = b2sSettings;
  }

  public boolean isDirectb2s() {
    return directb2s;
  }

  public void setDirectb2s(boolean directb2s) {
    this.directb2s = directb2s;
  }

  public boolean isRom() {
    return rom;
  }

  public void setRom(boolean rom) {
    this.rom = rom;
  }

  public boolean isNvRam() {
    return nvRam;
  }

  public void setNvRam(boolean nvRam) {
    this.nvRam = nvRam;
  }

  public boolean isRes() {
    return res;
  }

  public void setRes(boolean res) {
    this.res = res;
  }

  public boolean isVbs() {
    return vbs;
  }

  public void setVbs(boolean vbs) {
    this.vbs = vbs;
  }

  public boolean isPov() {
    return pov;
  }

  public void setPov(boolean pov) {
    this.pov = pov;
  }

  public boolean isIni() {
    return ini;
  }

  public void setIni(boolean ini) {
    this.ini = ini;
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

  public boolean isAltColor() {
    return altColor;
  }

  public void setAltColor(boolean altColor) {
    this.altColor = altColor;
  }

  public boolean isHighscore() {
    return highscore;
  }

  public void setHighscore(boolean highscore) {
    this.highscore = highscore;
  }

  public boolean isDmd() {
    return dmd;
  }

  public void setDmd(boolean dmd) {
    this.dmd = dmd;
  }

  public boolean isVpx() {
    return vpx;
  }

  public void setVpx(boolean vpx) {
    this.vpx = vpx;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.VPXZ_SETTINGS;
  }
}
