package de.mephisto.vpin.server.roms;

import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ScanResult {
  private int nvOffset = 0;

  private String rom;
  private String gameName;
  private String hsFileName;
  private String someTextFile;
  private String tableName;
  private String pupPackName;
  private boolean foundControllerStop = false;
  private boolean foundTableExit = false;
  private boolean vrRoomSupport = false;
  private boolean vrRoomDisabled = false;

  private String dmdType;
  private String dmdGameName;
  private String dmdProjectFolder;

  public boolean isVrRoomSupport() {
    return vrRoomSupport;
  }

  public void setVrRoomSupport(boolean vrRoomSupport) {
    this.vrRoomSupport = vrRoomSupport;
  }

  public boolean isVrRoomDisabled() {
    return vrRoomDisabled;
  }

  public void setVrRoomDisabled(boolean vrRoomDisabled) {
    this.vrRoomDisabled = vrRoomDisabled;
  }

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

  public String getGameName() {
    return gameName;
  }

  public void setGameName(String gameName) {
    this.gameName = gameName;
  }

  @Nullable
  public String getRom() {
    return rom;
  }

  public void setRom(@Nullable String rom) {
    this.rom = rom;
  }

  public String getDMDType() {
    return dmdType;
  }

  public void setDMDType(String dmdType) {
    this.dmdType = dmdType;
  }

  public String getDMDGameName() {
    return dmdGameName;
  }

  public void setDMDGameName(String dmdGameName) {
    this.dmdGameName = dmdGameName;
  }

  public String getDMDProjectFolder() {
    return dmdProjectFolder;
  }

  public void setDMDProjectFolder(String dmdProjectFolder) {
    this.dmdProjectFolder = dmdProjectFolder;
  }

  public boolean isScanComplete() {
    return this.nvOffset > 0 && this.rom != null && hsFileName != null;
  }
}
