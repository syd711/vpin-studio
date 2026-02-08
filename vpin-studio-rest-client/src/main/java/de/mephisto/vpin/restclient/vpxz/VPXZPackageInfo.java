package de.mephisto.vpin.restclient.vpxz;


import java.util.Objects;

public class VPXZPackageInfo {
  public final static String PACKAGE_INFO_JSON_FILENAME = "package-info.json";

  private VPXZFileInfo rom;
  private VPXZFileInfo nvRam;
  private VPXZFileInfo res;
  private VPXZFileInfo vbs;
  private VPXZFileInfo pov;
  private VPXZFileInfo ini;
  private VPXZFileInfo cfg;
  private VPXZFileInfo vpx;

  private String thumbnail;

  public VPXZFileInfo getCfg() {
    return cfg;
  }

  public void setCfg(VPXZFileInfo cfg) {
    this.cfg = cfg;
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

  public VPXZFileInfo getVpx() {
    return vpx;
  }

  public void setVpx(VPXZFileInfo vpx) {
    this.vpx = vpx;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    VPXZPackageInfo that = (VPXZPackageInfo) o;
    return Objects.equals(rom, that.rom) && Objects.equals(nvRam, that.nvRam) && Objects.equals(res, that.res) && Objects.equals(vbs, that.vbs) && Objects.equals(pov, that.pov) && Objects.equals(ini, that.ini) && Objects.equals(cfg, that.cfg) && Objects.equals(vpx, that.vpx) && Objects.equals(thumbnail, that.thumbnail);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rom, nvRam, res, vbs, pov, ini, cfg, vpx, thumbnail);
  }
}
