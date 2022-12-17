package de.mephisto.vpin.server.roms;

import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScanResult {
  private int nvOffset = 0;
  private String rom;
  private String hsFileName;

  private List<String> assets = new ArrayList<>();

  public List<String> getAssets() {
    return assets;
  }

  public void setAssets(List<String> assets) {
    this.assets = assets;
  }

  public String getHsFileName() {
    return hsFileName;
  }

  public void setHsFileName(String hsFileName) {
    this.hsFileName = hsFileName;
  }

  public int getNvOffset() {
    return nvOffset;
  }

  public void setNvOffset(int nvOffset) {
    this.nvOffset = nvOffset;
  }

  @Nullable
  public String getRom() {
    return rom;
  }

  public void setRom(@Nullable String rom) {
    this.rom = rom;
  }

  public boolean isScanComplete() {
    return this.nvOffset > 0 && this.rom != null && hsFileName != null;
  }
}
