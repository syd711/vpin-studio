package de.mephisto.vpin.server.roms;

import edu.umd.cs.findbugs.annotations.Nullable;

public class ScanResult {
  private int nvOffset = 0;
  private String rom;

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
    return this.nvOffset > 0 && this.rom != null;
  }
}
