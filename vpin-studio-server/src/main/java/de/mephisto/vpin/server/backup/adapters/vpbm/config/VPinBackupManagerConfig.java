package de.mephisto.vpin.server.backup.adapters.vpbm.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.mephisto.vpin.server.system.SystemService;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class VPinBackupManagerConfig {
  @JsonProperty("VpinballBasePath")
  private String vpinballBasePath = "C:\\vPinball";

  @JsonProperty("BackupPath")
  private String backupPath = new File(SystemService.ARCHIVES_FOLDER, "backups").getAbsolutePath();

  @JsonProperty("ExportPath")
  private String exportPath = new File(SystemService.ARCHIVES_FOLDER, "exports").getAbsolutePath();

  @JsonProperty("Emulators")
  private List<Emulator> emulators = Arrays.asList(new Emulator("Visual Pinball X"), new Emulator("Future Pinball"));

  @JsonProperty("LogLevel")
  private String logLevel = "Information";

  @JsonProperty("LogRotationSizeMb")
  private int logRotationSizeMb = 20;

  @JsonProperty("Pinup")
  private Pinup pinup = new Pinup();

  public Pinup getPinup() {
    return pinup;
  }

  public void setPinup(Pinup pinup) {
    this.pinup = pinup;
  }

  public String getVpinballBasePath() {
    return vpinballBasePath;
  }

  public void setVpinballBasePath(String vpinballBasePath) {
    this.vpinballBasePath = vpinballBasePath;
  }

  public String getBackupPath() {
    return backupPath;
  }

  public void setBackupPath(String backupPath) {
    this.backupPath = backupPath;
  }

  public String getExportPath() {
    return exportPath;
  }

  public void setExportPath(String exportPath) {
    this.exportPath = exportPath;
  }

  public List<Emulator> getEmulators() {
    return emulators;
  }

  public void setEmulators(List<Emulator> emulators) {
    this.emulators = emulators;
  }

  public String getLogLevel() {
    return logLevel;
  }

  public void setLogLevel(String logLevel) {
    this.logLevel = logLevel;
  }

  public int getLogRotationSizeMb() {
    return logRotationSizeMb;
  }

  public void setLogRotationSizeMb(int logRotationSizeMb) {
    this.logRotationSizeMb = logRotationSizeMb;
  }
}
