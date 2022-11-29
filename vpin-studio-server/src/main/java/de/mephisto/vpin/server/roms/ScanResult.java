package de.mephisto.vpin.server.roms;

import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScanResult {
  private int nvOffset = 0;
  private String rom;
  private String hsFileName;

  private List<String> music = new ArrayList<>();

  public List<String> getMusic() {
    return music;
  }

  public void setMusic(List<String> music) {
    this.music = music;
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
