package de.mephisto.vpin.restclient.vps;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.LaunchConfiguration;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.preferences.AutoFillSettings;

import java.util.ArrayList;
import java.util.List;

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

  private String authorDenyList;

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
