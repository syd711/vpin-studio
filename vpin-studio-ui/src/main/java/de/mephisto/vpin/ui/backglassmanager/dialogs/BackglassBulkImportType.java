package de.mephisto.vpin.ui.backglassmanager.dialogs;

import de.mephisto.vpin.restclient.frontend.VPinScreen;

/**
 * The kind of image that is extracted from the directb2s files and imported as media.
 */
public enum BackglassBulkImportType {
  BACKGLASS(VPinScreen.BackGlass),
  DMD(VPinScreen.Menu);

  private final VPinScreen screen;

  BackglassBulkImportType(VPinScreen screen) {
    this.screen = screen;
  }

  public VPinScreen getScreen() {
    return screen;
  }
}
