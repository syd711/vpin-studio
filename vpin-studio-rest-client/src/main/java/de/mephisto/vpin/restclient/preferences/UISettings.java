package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;

public class UISettings extends JsonSettings {
  private boolean hideVersions;
  private boolean hideVPSUpdates;

  private boolean hideComponentWarning;
  private boolean hideVPXStartInfo;

  private boolean hideDismissConfirmations;
  private boolean hideUpdateInfo;

  private boolean hideEmulatorColumn = true;

  private boolean autoFill = true;

  public boolean isAutoFill() {
    return autoFill;
  }

  public void setAutoFill(boolean autoFill) {
    this.autoFill = autoFill;
  }

  public boolean isHideEmulatorColumn() {
    return hideEmulatorColumn;
  }

  public void setHideEmulatorColumn(boolean hideEmulatorColumn) {
    this.hideEmulatorColumn = hideEmulatorColumn;
  }

  public boolean isHideVersions() {
    return hideVersions;
  }

  public void setHideVersions(boolean hideVersions) {
    this.hideVersions = hideVersions;
  }

  public boolean isHideVPSUpdates() {
    return hideVPSUpdates;
  }

  public void setHideVPSUpdates(boolean hideVPSUpdates) {
    this.hideVPSUpdates = hideVPSUpdates;
  }

  public boolean isHideComponentWarning() {
    return hideComponentWarning;
  }

  public void setHideComponentWarning(boolean hideComponentWarning) {
    this.hideComponentWarning = hideComponentWarning;
  }

  public boolean isHideDismissConfirmations() {
    return hideDismissConfirmations;
  }

  public void setHideDismissConfirmations(boolean hideDismissConfirmations) {
    this.hideDismissConfirmations = hideDismissConfirmations;
  }

  public boolean isHideVPXStartInfo() {
    return hideVPXStartInfo;
  }

  public void setHideVPXStartInfo(boolean hideVPXStartInfo) {
    this.hideVPXStartInfo = hideVPXStartInfo;
  }

  public boolean isHideUpdateInfo() {
    return hideUpdateInfo;
  }

  public void setHideUpdateInfo(boolean hideUpdateInfo) {
    this.hideUpdateInfo = hideUpdateInfo;
  }
}
