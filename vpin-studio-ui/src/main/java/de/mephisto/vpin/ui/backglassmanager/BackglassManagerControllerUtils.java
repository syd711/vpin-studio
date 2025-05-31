package de.mephisto.vpin.ui.backglassmanager;

import java.io.File;

import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressDialog;
import edu.umd.cs.findbugs.annotations.Nullable;

public class BackglassManagerControllerUtils {

    public static void updateDMDImage(int emulatorId, String b2sFileName, GameRepresentation game, File selection) {
    ProgressDialog.createProgressDialog(new BackglassManagerDmdUploadProgressModel("Set DMD Image", emulatorId, b2sFileName, selection));
    notifyChange(emulatorId, b2sFileName, game);
  }

  public static void deleteDMDImage(int emulatorId, String b2sFileName, GameRepresentation game) {
    ProgressDialog.createProgressDialog(new BackglassManagerDmdUploadProgressModel("Delete DMD Image", emulatorId, b2sFileName, null));
    notifyChange(emulatorId, b2sFileName, game);
  }

  public static void notifyChange(DirectB2S b2s) {
    if (b2s != null) {
      notifyChange(b2s.getEmulatorId(), b2s.getFileName(), b2s.getGameId());
    }
  }

  public static void notifyChange(int emulatorId, String b2sFileName, @Nullable GameRepresentation game) {
    notifyChange(emulatorId, b2sFileName, game != null ? game.getId() : -1);
  }

  public static void notifyChange(int emulatorId, String b2sFileName, int gameId) {
    // the tableChange notification will trigger a backglass change as well
    if (gameId > 0) {
      EventManager.getInstance().notifyTableChange(gameId, null);
    }
    else {
      EventManager.getInstance().notifyBackglassChange(emulatorId, b2sFileName);
    }
  }

}
