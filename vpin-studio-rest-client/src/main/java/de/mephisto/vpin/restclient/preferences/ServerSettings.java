package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

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
  private boolean initialMute = false;
  private String mappingVpsTableId = "WEBGameID";
  private String mappingVpsTableVersionId = "CUSTOM3";
  private String mappingHsFileName = "CUSTOM4";
  private String mappingPatchVersion = "CUSTOM5";
  private boolean stickyKeysEnabled = true;

  /** When virtual DMD is disabled, disable DMD in DmdDevice.ini */
  private boolean disableDmdViaIni = false;
  /** When virtual DMD is disabled, turn external dmd off in VpinMame */
  private boolean disableDmdInMame = true;
  /** For alphanumeric DMD, turn off backglass scores rendering */
  private boolean disableBackglassScore = true;

  public boolean isInitialMute() {
    return initialMute;
  }

  public void setInitialMute(boolean initialMute) {
    this.initialMute = initialMute;
  }

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

  public String getMappingPatchVersion() {
    return mappingPatchVersion;
  }

  public void setMappingPatchVersion(String mappingPatchVersion) {
    this.mappingPatchVersion = mappingPatchVersion;
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

  public boolean isDisableDmdViaIni() {
    return disableDmdViaIni;
  }

  public void setDisableDmdViaIni(boolean disableDmdViaIni) {
    this.disableDmdViaIni = disableDmdViaIni;
  }

  public boolean isDisableDmdInMame() {
    return disableDmdInMame;
  }

  public void setDisableDmdInMame(boolean disableDmdInMame) {
    this.disableDmdInMame = disableDmdInMame;
  }

  public boolean isDisableBackglassScore() {
    return disableBackglassScore;
  }

  public void setDisableBackglassScore(boolean disableBackglassScore) {
    this.disableBackglassScore = disableBackglassScore;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.SERVER_SETTINGS;
  }
}
