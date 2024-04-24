package de.mephisto.vpin.restclient.preferences;

import de.mephisto.vpin.restclient.JsonSettings;

public class UISettings extends JsonSettings {
  private boolean hideVersions;
  private boolean hideVPSUpdates;
  private boolean vpsAltSound = true;
  private boolean vpsAltColor = true;
  private boolean vpsBackglass = true;
  private boolean vpsPOV = true;
  private boolean vpsPUPPack = true;
  private boolean vpsRom = true;
  private boolean vpsSound = true;
  private boolean vpsToppper = true;
  private boolean vpsTutorial = true;
  private boolean vpsWheel = true;

  private boolean hideComponentWarning;
  private boolean hideVPXStartInfo;

  private boolean hideDismissConfirmations;
  private boolean hideUpdateInfo;

  private boolean hideEmulatorColumn = true;

  private boolean autoApplyVpsData = true;

  //open after upload
  private boolean autoEditTableData = true;

  private boolean propperAuthorField = true;
  private boolean propperModField = true;
  private boolean propperVersionField = true;
  private boolean propperVRField = true;

  private String winNetworkShare;

  public String getWinNetworkShare() {
    return winNetworkShare;
  }

  public void setWinNetworkShare(String winNetworkShare) {
    this.winNetworkShare = winNetworkShare;
  }

  public boolean isAutoApplyVpsData() {
    return autoApplyVpsData;
  }

  public void setAutoApplyVpsData(boolean autoApplyVpsData) {
    this.autoApplyVpsData = autoApplyVpsData;
  }

  public boolean isPropperAuthorField() {
    return propperAuthorField;
  }

  public void setPropperAuthorField(boolean propperAuthorField) {
    this.propperAuthorField = propperAuthorField;
  }

  public boolean isPropperModField() {
    return propperModField;
  }

  public void setPropperModField(boolean propperModField) {
    this.propperModField = propperModField;
  }

  public boolean isPropperVersionField() {
    return propperVersionField;
  }

  public void setPropperVersionField(boolean propperVersionField) {
    this.propperVersionField = propperVersionField;
  }

  public boolean isPropperVRField() {
    return propperVRField;
  }

  public void setPropperVRField(boolean propperVRField) {
    this.propperVRField = propperVRField;
  }

  public boolean isAutoEditTableData() {
    return autoEditTableData;
  }

  public void setAutoEditTableData(boolean autoEditTableData) {
    this.autoEditTableData = autoEditTableData;
  }

  public boolean isVpsWheel() {
    return vpsWheel;
  }

  public void setVpsWheel(boolean vpsWheel) {
    this.vpsWheel = vpsWheel;
  }

  public boolean isVpsAltSound() {
    return vpsAltSound;
  }

  public void setVpsAltSound(boolean vpsAltSound) {
    this.vpsAltSound = vpsAltSound;
  }

  public boolean isVpsAltColor() {
    return vpsAltColor;
  }

  public void setVpsAltColor(boolean vpsAltColor) {
    this.vpsAltColor = vpsAltColor;
  }

  public boolean isVpsBackglass() {
    return vpsBackglass;
  }

  public void setVpsBackglass(boolean vpsBackglass) {
    this.vpsBackglass = vpsBackglass;
  }

  public boolean isVpsPOV() {
    return vpsPOV;
  }

  public void setVpsPOV(boolean vpsPOV) {
    this.vpsPOV = vpsPOV;
  }

  public boolean isVpsPUPPack() {
    return vpsPUPPack;
  }

  public void setVpsPUPPack(boolean vpsPUPPack) {
    this.vpsPUPPack = vpsPUPPack;
  }

  public boolean isVpsRom() {
    return vpsRom;
  }

  public void setVpsRom(boolean vpsRom) {
    this.vpsRom = vpsRom;
  }

  public boolean isVpsSound() {
    return vpsSound;
  }

  public void setVpsSound(boolean vpsSound) {
    this.vpsSound = vpsSound;
  }

  public boolean isVpsToppper() {
    return vpsToppper;
  }

  public void setVpsToppper(boolean vpsToppper) {
    this.vpsToppper = vpsToppper;
  }

  public boolean isVpsTutorial() {
    return vpsTutorial;
  }

  public void setVpsTutorial(boolean vpsTutorial) {
    this.vpsTutorial = vpsTutorial;
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
