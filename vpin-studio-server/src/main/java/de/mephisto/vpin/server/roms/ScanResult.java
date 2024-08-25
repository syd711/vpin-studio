package de.mephisto.vpin.server.roms;

import de.mephisto.vpin.restclient.JsonSettings;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScanResult {
  private int nvOffset = 0;
  private String rom;
  private String hsFileName;
  private String someTextFile;
  private String tableName;
  private String pupPackName;
  private boolean foundControllerStop = false;
  private boolean foundTableExit = false;

  public boolean isFoundTableExit() {
    return foundTableExit;
  }

  public void setFoundTableExit(boolean foundTableExit) {
    this.foundTableExit = foundTableExit;
  }

  public boolean isFoundControllerStop() {
    return foundControllerStop;
  }

  public void setFoundControllerStop(boolean foundControllerStop) {
    this.foundControllerStop = foundControllerStop;
  }

  public String getPupPackName() {
    return pupPackName;
  }

  public void setPupPackName(String pupPackName) {
    this.pupPackName = pupPackName;
  }

  public String getSomeTextFile() {
    return someTextFile;
  }

  public void setSomeTextFile(String someTextFile) {
    this.someTextFile = someTextFile;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

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
    while (hsFileName.contains("\\")) {
      hsFileName = hsFileName.substring(hsFileName.indexOf("\\") + 1);
    }
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
