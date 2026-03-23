package de.mephisto.vpin.restclient.vr;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;

public class VRFilesInfo {
  private int emulatorId;

  private File dmdDeviceIniFile;
  private File dmdDeviceIniVrFile;

  private File vPinballXIniFile;
  private File vPinballXIniVrFile;

  public int getEmulatorId() {
    return emulatorId;
  }

  public void setEmulatorId(int emulatorId) {
    this.emulatorId = emulatorId;
  }

  @JsonIgnore
  public File getDmdDeviceIniFile() {
    return dmdDeviceIniFile;
  }

  public String getDmdDeviceIni() {
    if (dmdDeviceIniFile != null) {
      return dmdDeviceIniFile.getAbsolutePath();
    }
    return null;
  }

  public void setDmdDeviceIniFile(File dmdDeviceIniFile) {
    this.dmdDeviceIniFile = dmdDeviceIniFile;
  }

  @JsonIgnore
  public File getDmdDeviceIniVrFile() {
    return dmdDeviceIniVrFile;
  }

  public String getDmdDeviceIniVr() {
    if (dmdDeviceIniVrFile != null) {
      return dmdDeviceIniVrFile.getAbsolutePath();
    }
    return null;
  }

  public void setDmdDeviceIniVrFile(File dmdDeviceIniVrFile) {
    this.dmdDeviceIniVrFile = dmdDeviceIniVrFile;
  }

  @JsonIgnore
  public File getvPinballXIniFile() {
    return vPinballXIniFile;
  }

  public String getvPinballXIni() {
    if (vPinballXIniFile != null) {
      return vPinballXIniFile.getAbsolutePath();
    }
    return null;
  }

  public void setvPinballXIniFile(File vPinballXIniFile) {
    this.vPinballXIniFile = vPinballXIniFile;
  }

  @JsonIgnore
  public File getvPinballXIniVrFile() {
    return vPinballXIniVrFile;
  }

  public String getvPinballXIniVr() {
    if (vPinballXIniVrFile != null) {
      return vPinballXIniVrFile.getAbsolutePath();
    }
    return null;
  }

  public void setvPinballXIniVrFile(File vPinballXIniVrFile) {
    this.vPinballXIniVrFile = vPinballXIniVrFile;
  }
}
