package de.mephisto.vpin.ui.recorder;

import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import de.mephisto.vpin.ui.tables.TableFilterController;

public class RecorderFilterController extends TableFilterController {
  protected GameEmulatorRepresentation getEmulatorSelection() {
    return ((RecorderController) tableController).getEmulatorSelection();
  }
}
