package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

public class VPXZSettings extends JsonSettings {
  private boolean rom = true;
  private boolean nvRam = true;
  private boolean res = true;
  private boolean vbs = true;
  private boolean pov = true;
  private boolean ini = true;
  private boolean vpx = true;

  private boolean isOverwriteFile = true;
  private String webserverHost;
  private int webserverPort = 2112;

  private boolean enabled = false;

  public String getWebserverHost() {
    return webserverHost;
  }

  public void setWebserverHost(String webserverHost) {
    this.webserverHost = webserverHost;
  }

  public int getWebserverPort() {
    return webserverPort;
  }

  public void setWebserverPort(int webserverPort) {
    this.webserverPort = webserverPort;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isOverwriteFile() {
    return isOverwriteFile;
  }

  public void setOverwriteFile(boolean overwriteFile) {
    this.isOverwriteFile = overwriteFile;
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
