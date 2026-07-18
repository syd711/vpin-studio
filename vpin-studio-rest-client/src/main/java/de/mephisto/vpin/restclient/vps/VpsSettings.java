package de.mephisto.vpin.restclient.vps;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;

public class VpsSettings extends JsonSettings {
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

  private boolean vpsColumnAltSound = true;
  private boolean vpsColumnAltColor = true;
  private boolean vpsColumnBackglass = true;
  private boolean vpsColumnPOV = true;
  private boolean vpsColumnPUPPack = true;
  private boolean vpsColumnRom = true;
  private boolean vpsColumnSound = true;
  private boolean vpsColumnToppper = true;
  private boolean vpsColumnTutorial = true;
  private boolean vpsColumnWheel = true;

  private String authorDenyList;

  public boolean isVpsColumnAltSound() {
    return vpsColumnAltSound;
  }

  public void setVpsColumnAltSound(boolean vpsColumnAltSound) {
    this.vpsColumnAltSound = vpsColumnAltSound;
  }

  public boolean isVpsColumnAltColor() {
    return vpsColumnAltColor;
  }

  public void setVpsColumnAltColor(boolean vpsColumnAltColor) {
    this.vpsColumnAltColor = vpsColumnAltColor;
  }

  public boolean isVpsColumnBackglass() {
    return vpsColumnBackglass;
  }

  public void setVpsColumnBackglass(boolean vpsColumnBackglass) {
    this.vpsColumnBackglass = vpsColumnBackglass;
  }

  public boolean isVpsColumnPOV() {
    return vpsColumnPOV;
  }

  public void setVpsColumnPOV(boolean vpsColumnPOV) {
    this.vpsColumnPOV = vpsColumnPOV;
  }

  public boolean isVpsColumnPUPPack() {
    return vpsColumnPUPPack;
  }

  public void setVpsColumnPUPPack(boolean vpsColumnPUPPack) {
    this.vpsColumnPUPPack = vpsColumnPUPPack;
  }

  public boolean isVpsColumnRom() {
    return vpsColumnRom;
  }

  public void setVpsColumnRom(boolean vpsColumnRom) {
    this.vpsColumnRom = vpsColumnRom;
  }

  public boolean isVpsColumnSound() {
    return vpsColumnSound;
  }

  public void setVpsColumnSound(boolean vpsColumnSound) {
    this.vpsColumnSound = vpsColumnSound;
  }

  public boolean isVpsColumnToppper() {
    return vpsColumnToppper;
  }

  public void setVpsColumnToppper(boolean vpsColumnToppper) {
    this.vpsColumnToppper = vpsColumnToppper;
  }

  public boolean isVpsColumnTutorial() {
    return vpsColumnTutorial;
  }

  public void setVpsColumnTutorial(boolean vpsColumnTutorial) {
    this.vpsColumnTutorial = vpsColumnTutorial;
  }

  public boolean isVpsColumnWheel() {
    return vpsColumnWheel;
  }

  public void setVpsColumnWheel(boolean vpsColumnWheel) {
    this.vpsColumnWheel = vpsColumnWheel;
  }

  public String getAuthorDenyList() {
    return authorDenyList;
  }

  public void setAuthorDenyList(String authorDenyList) {
    this.authorDenyList = authorDenyList;
  }

  public boolean isHideVPSUpdates() {
    return hideVPSUpdates;
  }

  public void setHideVPSUpdates(boolean hideVPSUpdates) {
    this.hideVPSUpdates = hideVPSUpdates;
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

  public boolean isVpsWheel() {
    return vpsWheel;
  }

  public void setVpsWheel(boolean vpsWheel) {
    this.vpsWheel = vpsWheel;
  }

  @Override
  public String getSettingsName() {
    return PreferenceNames.VPS_SETTINGS;
  }
}
