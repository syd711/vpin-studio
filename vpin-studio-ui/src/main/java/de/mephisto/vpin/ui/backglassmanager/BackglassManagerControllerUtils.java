package de.mephisto.vpin.ui.backglassmanager;

import java.io.File;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressDialog;

public class BackglassManagerControllerUtils {

    public static void updateDMDImage(int emulatorId, String b2sFileName, GameRepresentation game, File selection) {
    ProgressDialog.createProgressDialog(new BackglassManagerDmdUploadProgressModel("Set DMD Image", emulatorId, b2sFileName, selection));

    // then refresh images and table
    EventManager.getInstance().notifyBackglassChange(emulatorId, b2sFileName);

    if (game != null) {
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    }
  }

  public static void deleteDMDImage(int emulatorId, String b2sFileName, GameRepresentation game) {
    ProgressDialog.createProgressDialog(new BackglassManagerDmdUploadProgressModel("Delete DMD Image", emulatorId, b2sFileName, null));

    // then refresh images and table
    EventManager.getInstance().notifyBackglassChange(emulatorId, b2sFileName);

    if (game != null) {
      EventManager.getInstance().notifyTableChange(game.getId(), null);
    }
  }

}
