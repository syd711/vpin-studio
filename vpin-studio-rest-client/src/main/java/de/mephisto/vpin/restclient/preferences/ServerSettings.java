package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;

public class ServerSettings extends JsonSettings {
  private boolean vpxKeepFileNames;
  private boolean vpxKeepDisplayNames;
  private boolean backupTableOnOverwrite = true;
  private boolean keepVbsFiles = true;
  private boolean keepModificationDate = false;
  private boolean useSubfolders = false;
  private boolean launchPopperOnExit = false;
  private boolean useVPXTableMonitor = false;
  private int volume = 0;
  private String mappingVpsTableId = "WEBGameID";
  private String mappingVpsTableVersionId = "CUSTOM3";
  private String mappingHsFileName = "CUSTOM4";
  private boolean stickyKeysEnabled = true;

  public boolean isStickyKeysEnabled() {
    return stickyKeysEnabled;
  }

  public void setStickyKeysEnabled(boolean stickyKeysEnabled) {
    this.stickyKeysEnabled = stickyKeysEnabled;
  }

  public boolean isKeepModificationDate() {
    return keepModificationDate;
  }

  public void setKeepModificationDate(boolean keepModificationDate) {
    this.keepModificationDate = keepModificationDate;
  }

  public boolean isUseVPXTableMonitor() {
    return useVPXTableMonitor;
  }

  public void setUseVPXTableMonitor(boolean useVPXTableMonitor) {
    this.useVPXTableMonitor = useVPXTableMonitor;
  }

  public boolean isUseSubfolders() {
    return useSubfolders;
  }

  public void setUseSubfolders(boolean useSubfolders) {
    this.useSubfolders = useSubfolders;
  }

  public int getVolume() {
    return volume;
  }

  public void setVolume(int volume) {
    this.volume = volume;
  }

  public boolean isKeepVbsFiles() {
    return keepVbsFiles;
  }

  public void setKeepVbsFiles(boolean keepVbsFiles) {
    this.keepVbsFiles = keepVbsFiles;
  }

  public boolean isBackupTableOnOverwrite() {
    return backupTableOnOverwrite;
  }

  public void setBackupTableOnOverwrite(boolean backupTableOnOverwrite) {
    this.backupTableOnOverwrite = backupTableOnOverwrite;
  }

  public String getMappingVpsTableVersionId() {
    return mappingVpsTableVersionId;
  }

  public void setMappingVpsTableVersionId(String mappingVpsTableVersionId) {
    this.mappingVpsTableVersionId = mappingVpsTableVersionId;
  }

  public String getMappingVpsTableId() {
    return mappingVpsTableId;
  }

  public void setMappingVpsTableId(String mappingVpsTableId) {
    this.mappingVpsTableId = mappingVpsTableId;
  }

  public String getMappingHsFileName() {
    return mappingHsFileName;
  }

  public void setMappingHsFileName(String mappingHsFileName) {
    this.mappingHsFileName = mappingHsFileName;
  }

  public boolean isLaunchPopperOnExit() {
    return launchPopperOnExit;
  }

  public void setLaunchPopperOnExit(boolean launchPopperOnExit) {
    this.launchPopperOnExit = launchPopperOnExit;
  }

  public boolean isVpxKeepFileNames() {
    return vpxKeepFileNames;
  }

  public void setVpxKeepFileNames(boolean vpxKeepFileNames) {
    this.vpxKeepFileNames = vpxKeepFileNames;
  }

  public boolean isVpxKeepDisplayNames() {
    return vpxKeepDisplayNames;
  }

  public void setVpxKeepDisplayNames(boolean vpxKeepDisplayNames) {
    this.vpxKeepDisplayNames = vpxKeepDisplayNames;
  }
}
