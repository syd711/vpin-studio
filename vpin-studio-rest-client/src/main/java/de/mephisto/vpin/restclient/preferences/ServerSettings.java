package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;

public class ServerSettings extends JsonSettings {
  private boolean vpxKeepFileNames;
  private boolean vpxKeepDisplayNames;
  private boolean launchPopperOnExit = true;
  private String mappingHsFileName = "MediaSearch";
  private String mappingVpsTableId = "CUSTOM2";
  private String mappingVpsTableVersionId = "CUSTOM3";

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
