package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.backups.AuthenticationProvider;

public class BackupSettings extends JsonSettings {
  private AuthenticationProvider authenticationProvider;

  private boolean directb2s = true;
  private boolean pupPack = true;
  private boolean rom = true;
  private boolean nvRam = true;
  private boolean res = true;
  private boolean vbs = true;
  private boolean frontendMedia = true;
  private boolean pov = true;
  private boolean ini = true;
  private boolean music = true;
  private boolean altSound = true;
  private boolean altColor = true;
  private boolean highscore = true;
  private boolean dmd = true;
  private boolean vpx = true;
  private boolean registryData = true;

  public AuthenticationProvider getAuthenticationProvider() {
    return authenticationProvider;
  }

  public void setAuthenticationProvider(AuthenticationProvider authenticationProvider) {
    this.authenticationProvider = authenticationProvider;
  }

  public boolean isAssetEnabled(AssetType assetType) {
    switch (assetType) {
      case DIRECTB2S: {
        return directb2s;
      }
      case PUP_PACK: {
        return pupPack;
      }
      case NV: {
        return nvRam;
      }
      case RES: {
        return res;
      }
      case VBS: {
        return vbs;
      }
      case FRONTEND_MEDIA: {
        return frontendMedia;
      }
      case POV: {
        return pov;
      }
      case INI: {
        return ini;
      }
      case MUSIC:
      case MUSIC_BUNDLE: {
        return music;
      }
      case ALT_SOUND: {
        return altSound;
      }
      case ALT_COLOR: {
        return altColor;
      }
      case DMD_PACK: {
        return dmd;
      }
      case VPX: {
        return vpx;
      }
      case ROM: {
        return rom;
      }
    }
    return false;
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

  public boolean isFrontendMedia() {
    return frontendMedia;
  }

  public void setFrontendMedia(boolean frontendMedia) {
    this.frontendMedia = frontendMedia;
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

  public boolean isRegistryData() {
    return registryData;
  }

  public void setRegistryData(boolean registryData) {
    this.registryData = registryData;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.BACKUP_SETTINGS;
  }
}
