package de.mephisto.vpin.ui.backglassmanager;

import java.io.File;

import de.mephisto.vpin.restclient.directb2s.BackglassFileType;
import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressDialog;
import org.jspecify.annotations.Nullable;

public class BackglassManagerControllerUtils {

  /**
   * Returns the icon literal of a backglass file, depending on its type (.directb2s or .B2SPro)
   * and the number of available versions.
   */
  public static String getBackglassIconLiteral(@Nullable String b2sFileName, int nbVersions) {
    boolean b2sPro = BackglassFileType.isB2SPro(b2sFileName);
    if (nbVersions > 9) {
      return b2sPro ? "mdi2n-numeric-9-plus-box-multiple" : "mdi2n-numeric-9-plus-box-multiple-outline";
    }
    if (nbVersions > 1) {
      return "mdi2n-numeric-" + nbVersions + (b2sPro ? "-box-multiple" : "-box-multiple-outline");
    }
    return b2sPro ? "mdi2f-file-star-outline" : "mdi2f-file-document-edit-outline";
  }

  /**
   * Returns the label of the backglass file type, used for tooltips
   */
  public static String getBackglassTypeLabel(@Nullable String b2sFileName) {
    return BackglassFileType.isB2SPro(b2sFileName) ? "B2SPro Backglass" : "DirectB2S Backglass";
  }

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
