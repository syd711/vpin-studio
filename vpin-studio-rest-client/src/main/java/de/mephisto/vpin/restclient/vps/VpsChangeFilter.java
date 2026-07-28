package de.mephisto.vpin.restclient.vps;

import de.mephisto.vpin.connectors.vps.model.VPSChange;
import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;

/**
 * Shared logic for deciding whether a {@link VPSChange} is hidden by the user's {@link VpsSettings},
 * used both by the UI (VPS update column rendering) and the server (table filtering).
 */
public class VpsChangeFilter {

  public static boolean isFiltered(VpsSettings vpsSettings, VPSChange change) {
    if (vpsSettings != null) {
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.b2s) && !vpsSettings.isVpsBackglass()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.topper) && !vpsSettings.isVpsToppper()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.pov) && !vpsSettings.isVpsPOV()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.rom) && !vpsSettings.isVpsRom()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.altColor) && !vpsSettings.isVpsAltColor()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.altSound) && !vpsSettings.isVpsAltSound()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.pupPack) && !vpsSettings.isVpsPUPPack()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.sound) && !vpsSettings.isVpsSound()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.wheel) && !vpsSettings.isVpsWheel()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.tutorial) && !vpsSettings.isVpsTutorial()) {
        return true;
      }
    }
    return false;
  }
}
