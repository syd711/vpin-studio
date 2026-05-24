package de.mephisto.vpin.restclient.vr;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;

public class VRFilesInfo {
  private File dmdDeviceIniFile;
  private File dmdDeviceIniVrFile;

  private File vPinballXIniFile;
  private File vPinballXIniVrFile;

  private String dmdDeviceIni;
  private String dmdDeviceIniVr;

  @JsonProperty("vPinballXIni")
  private String vPinballXIni;

  @JsonProperty("vPinballXIniVr")
  private String vPinballXIniVr;

  @JsonIgnore
  public File getDmdDeviceIniFile() {
    return dmdDeviceIniFile;
  }

  public void setDmdDeviceIniFile(File dmdDeviceIniFile) {
    this.dmdDeviceIniFile = dmdDeviceIniFile;
  }

  @JsonIgnore
  public File getDmdDeviceIniVrFile() {
    return dmdDeviceIniVrFile;
  }

  public void setDmdDeviceIniVrFile(File dmdDeviceIniVrFile) {
    this.dmdDeviceIniVrFile = dmdDeviceIniVrFile;
  }

  @JsonIgnore
  public File getvPinballXIniFile() {
    return vPinballXIniFile;
  }

  public void setvPinballXIniFile(File vPinballXIniFile) {
    this.vPinballXIniFile = vPinballXIniFile;
  }

  @JsonIgnore
  public File getvPinballXIniVrFile() {
    return vPinballXIniVrFile;
  }

  public void setvPinballXIniVrFile(File vPinballXIniVrFile) {
    this.vPinballXIniVrFile = vPinballXIniVrFile;
  }

  public String getDmdDeviceIni() {
    return dmdDeviceIni;
  }

  public void setDmdDeviceIni(String dmdDeviceIni) {
    this.dmdDeviceIni = dmdDeviceIni;
  }

  public String getDmdDeviceIniVr() {
    return dmdDeviceIniVr;
  }

  public void setDmdDeviceIniVr(String dmdDeviceIniVr) {
    this.dmdDeviceIniVr = dmdDeviceIniVr;
  }


  public String getvPinballXIni() {
    return vPinballXIni;
  }

  public void setvPinballXIni(String vPinballXIni) {
    this.vPinballXIni = vPinballXIni;
  }

  public String getvPinballXIniVr() {
    return vPinballXIniVr;
  }

  public void setvPinballXIniVr(String vPinballXIniVr) {
    this.vPinballXIniVr = vPinballXIniVr;
  }
}
